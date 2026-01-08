package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.compound.model.FindSamplesRequest;
import com.epam.indigoeln.compound.model.SampleDTO;
import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.ReactionMutation;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import com.epam.indigoeln.reaction.util.PatchTestUtil;
import com.epam.indigoeln.test.FeignUtil;
import com.google.common.math.Stats;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import java.util.*;

public abstract class MutationsTestBase extends ELNBaseTest {

    protected NotebookDetailsDTO notebook;
    protected ExperimentDetailsDTO experiment;
    protected Reaction reaction;
    protected ReactionInput input1;
    protected ReactionInputSample input1Sample1;
    protected ReactionInput input2;
    protected ReactionInputSample input2Sample1;
    protected ReactionOutput output1;
    protected ReactionOutputSample output1Sample1;
    protected ReactionOutput output2;
    protected ReactionOutputSample output2Sample1;

    protected CalculationReportBuilder reportBuilder;
    private byte @Nullable [] picture = null;

    private List<Integer> modelSizes = new ArrayList<>();
    private List<Integer> patchSizes = new ArrayList<>();

    @BeforeAll
    void beforeAll(TestInfo testInfo) {
        withUser(JOHN_USERNAME, () -> {
            ProjectDetailsDTO project = projectClient.createProject(new ProjectRequest(testInfo.getDisplayName()));
            notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        });
    }

    @AfterAll
    void tearDownAllBase() {
        if (!modelSizes.isEmpty()) {
            Stats modelSizeStats = Stats.of(modelSizes);
            Stats patchSizeStats = Stats.of(patchSizes);
            System.out.println("Model size stats: " + modelSizeStats);
            System.out.println("Patch size stats: " + patchSizeStats);
            System.out.println("Average ratio: " + patchSizeStats.mean() / modelSizeStats.mean());
        }
    }

    protected void initExperiment(String projectName) {
        ProjectDetailsDTO project = getOrCreateProject(projectName);
        notebook = notebookClient.createNotebook(project.getId(), new NotebookRequest(nextNotebookName()));
        experiment = experimentClient.createExperiment(notebook.getId(), new ExperimentRequest(emptyTemplateID));
        modelUpdated();
    }

    @SuppressWarnings({"SizeReplaceableByIsEmpty", "DataFlowIssue", "SequencedCollectionMethodCanBeUsed"})
    protected void modelUpdated() {
        reaction = experiment.getModel().getReactions().getFirst();
        input1 = reaction.getInputs().size() >= 1 ? reaction.getInputs().get(0) : null;
        input1Sample1 = input1 != null && input1.getSamples().size() >= 1 ? input1.getSamples().get(0) : null;
        input2 = reaction.getInputs().size() >= 2 ? reaction.getInputs().get(1) : null;
        input2Sample1 = input2 != null && input2.getSamples().size() >= 1 ? input2.getSamples().get(0) : null;
        output1 = reaction.getOutputs().size() >= 1 ? reaction.getOutputs().get(0) : null;
        output1Sample1 = output1 != null && output1.getSamples().size() >= 1 ? output1.getSamples().get(0) : null;
        output2 = reaction.getOutputs().size() >= 2 ? reaction.getOutputs().get(1) : null;
        output2Sample1 = output2 != null && output2.getSamples().size() >= 1 ? output2.getSamples().get(0) : null;
    }

    protected void applyMutation(Mutation mutation) {
        applyMutation(mutation, true);
    }

    @SneakyThrows
    protected void applyMutation(Mutation mutation, boolean undoRedo) {
        System.out.println("Applying mutation: " + mutation);
        reportBuilder.addMutation(mutation);

        ExperimentPatch patch = experimentClient.mutateExperimentModel2(experiment.getId(), experiment.getRevision(), mutation);
        ExperimentDetailsDTO updatedExperiment = experimentClient.getExperiment(experiment.getId());

        // reload picture
        Response pictureResponse = experimentClient.getExperimentPictureClient(experiment.getId());
        byte[] newPicture = (byte[]) pictureResponse.getEntity();
        if (picture == null || newPicture != null && !Arrays.equals(picture, newPicture)) {
            picture = newPicture;
            reportBuilder.addPicture(picture, pictureResponse.getHeaderString(HttpHeaders.CONTENT_TYPE));
        }
        ExperimentSnapshot updatedSnapshot = experimentClient.getExperimentSnapshot(experiment.getId());
        reportBuilder.addModel(FeignUtil.OBJECT_MAPPER_FORMATTED.writeValueAsString(patch), updatedSnapshot);

        // verify if patch is correct
        PatchTestUtil.verifyModelPatch(experiment, patch, updatedExperiment, reportBuilder);
        experiment = updatedExperiment;

        // verify if undo/redo works and produces the same snapshot
        Integer initialRevision = experiment.getRevision();
        if (undoRedo) {
            applyMutation(new ExperimentMutation.Undo(initialRevision), false);
            applyMutation(new ExperimentMutation.Redo(initialRevision), false);
        }
        experiment = experimentClient.getExperiment(experiment.getId());
        ExperimentSnapshot snapshotAfterRedo = experimentClient.getExperimentSnapshot(experiment.getId());
        // revision and rxnVersion will be different after redo, restore them
        snapshotAfterRedo.setRevision(updatedSnapshot.getRevision());
        Iterator<Reaction> updatedReaction = updatedSnapshot.getModel().getReactions().iterator();
        for (Reaction value : snapshotAfterRedo.getModel().getReactions()) {
            value.setRxnVersion(updatedReaction.next().getRxnVersion());
        }
        PatchTestUtil.verifyModel(snapshotAfterRedo, updatedSnapshot, reportBuilder);

        modelUpdated();

        modelSizes.add(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(updatedExperiment).length);
        patchSizes.add(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(patch).length);
    }

    protected ReactionMutation.ResolveInputs prepareResolveInputs() {
        ReactionMutation.ResolveInputs mutation = new ReactionMutation.ResolveInputs(reaction.getAnchor(), new HashMap<>());
        Map<Anchor.Input, @Nullable FindSamplesRequest> requests = experimentClient.analyzeRXN(experiment.getId(), experiment.getModel().getReactions().getFirst().getAnchor());
        requests.forEach((anchor, request) -> {
            if (request != null) {
                Page<SampleDTO> samples = compoundClient.findSamples(request, Paging.DEFAULT);
                if (!samples.getItems().isEmpty()) {
                    mutation.inputSamples().put(anchor, samples.getItems().getFirst().getId());
                }
            }
        });
        return mutation;
    }
}
