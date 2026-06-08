package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.util.CalculationReportBuilder;
import com.epam.indigoeln.reaction.util.PatchTestUtil;
import com.epam.indigoeln.test.FeignUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.math.Stats;
import lombok.SneakyThrows;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class MutationsTestBase extends ELNBaseTest {

    protected NotebookDetailsDTO notebook;
    protected ExperimentDetailsDTO experiment;
    protected Reaction reaction;
    protected MutationResponse lastMutationResponse;
    protected ReactionInput input1;
    protected ReactionInputSample input1Sample1;
    protected ReactionInput input2;
    protected ReactionInputSample input2Sample1;
    protected ReactionInput input3;
    protected ReactionInput input4;
    protected ReactionOutput output1;
    protected ReactionOutputSample output1Sample1;
    protected ReactionOutput output2;
    protected ReactionOutputSample output2Sample1;
    protected ReactionOutput output3;
    protected ReactionOutputSample output3Sample1;

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

    @SuppressWarnings("DataFlowIssue")
    protected void modelUpdated() {
        reaction = experiment.getModel().getReactions().getFirst();
        input1 = safeGet(reaction.getInputs(), 0);
        input1Sample1 = input1 != null ? safeGet(input1.getSamples(), 0) : null;
        input2 = safeGet(reaction.getInputs(), 1);
        input2Sample1 = input2 != null ? safeGet(input2.getSamples(), 0) : null;
        input3 = safeGet(reaction.getInputs(), 2);
        input4 = safeGet(reaction.getInputs(), 3);
        output1 = safeGet(reaction.getOutputs(), 0);
        output1Sample1 = output1 != null ? safeGet(output1.getSamples(), 0) : null;
        output2 = safeGet(reaction.getOutputs(), 1);
        output2Sample1 = output2 != null ? safeGet(output2.getSamples(), 0) : null;
        output3 = safeGet(reaction.getOutputs(), 2);
        output3Sample1 = output3 != null ? safeGet(output3.getSamples(), 0) : null;
    }

    @Nullable
    private <T> T safeGet(List<T> list, int index) {
        return list.size() > index ? list.get(index) : null;
    }

    protected MutationResponse applyMutation(Mutation mutation) {
        boolean undoOrRedo = mutation instanceof ExperimentMutation.Undo || mutation instanceof ExperimentMutation.Redo;
        return applyMutation(mutation, !undoOrRedo);
    }

    protected MutationResponse applyMutation(Supplier<MutationResponse> executor, Supplier<String> mutationStr) {
        return applyMutation(executor, mutationStr, "mutation", true);
    }

    protected MutationResponse applyMutation(Mutation mutation, boolean undoRedo) {
        return applyMutation(mutation, "mutation", undoRedo);
    }

    protected MutationResponse applyMutation(Supplier<MutationResponse> executor, Supplier<String> mutationStr, boolean undoRedo) {
        return applyMutation(executor, mutationStr, "mutation", undoRedo);
    }

    protected MutationResponse applyMutation(Mutation mutation, String reportClass, boolean undoRedo) {
        return applyMutation(
                () -> experimentClient.mutateExperimentModel4(experiment.getId(), experiment.getRevision(), mutation),
                mutation::toString,
                "mutation",
                undoRedo
        );
    }

    @SneakyThrows
    protected MutationResponse applyMutation(Supplier<MutationResponse> executor, Supplier<String> mutationStr, String reportClass, boolean undoRedo) {
        System.out.println("Applying mutation: " + mutationStr.get());
        reportBuilder.addMutation(reportClass, mutationStr.get());

        ExperimentSnapshot initialSnapshot = experimentClient.getExperimentSnapshot(experiment.getId());
        MutationResponse response = executor.get();
        lastMutationResponse = response;
        JsonNode patch = response.getPatch();
        ExperimentDetailsDTO updatedExperiment = experimentClient.getExperiment(experiment.getId());

        //noinspection ConstantValue
        String newPicture = response.getReactionImages() != null ? response.getReactionImages().get(reaction.getAnchor()) : null;
        if (newPicture != null) {
            picture = newPicture.getBytes();
            reportBuilder.addPicture(reportClass, picture, "image/svg+xml");
        }
        ExperimentSnapshot updatedSnapshot = experimentClient.getExperimentSnapshot(experiment.getId());

        //noinspection ConstantValue
        if (response.getMessages() != null) {
            for (String message : response.getMessages()) {
                reportBuilder.addMessage("", "Message: " + message);
            }
        }
        reportBuilder.addModel(reportClass, initialSnapshot, patch, updatedSnapshot);

        // verify if patch is correct
        PatchTestUtil.verifyModelPatch(experiment, patch, updatedExperiment, reportBuilder);
        // verify patch reverse is correct
        PatchTestUtil.verifyReversePatch(experiment, patch, updatedExperiment, reportBuilder);

        experiment = updatedExperiment;
        if (undoRedo) {
            // verify if model after undo is the same as before initial mutation
            applyMutation(new ExperimentMutation.Undo(), "undo", false);
            ExperimentSnapshot snapshotAfterUndo = experimentClient.getExperimentSnapshot(experiment.getId());
            PatchTestUtil.verifyModel(snapshotAfterUndo, initialSnapshot, reportBuilder, () -> "Model after undo (right) not equals to model before initial operation (left)");

            // verify if undo+redo works and produces the same snapshot as initial mutation
            applyMutation(new ExperimentMutation.Redo(), "redo", false);
            experiment = experimentClient.getExperiment(experiment.getId());
            ExperimentSnapshot snapshotAfterRedo = experimentClient.getExperimentSnapshot(experiment.getId());

            PatchTestUtil.verifyModel(snapshotAfterRedo, updatedSnapshot, reportBuilder, () -> "Model after redo (right) not equals to model after initial operation (left)");
        }

        modelUpdated();

        modelSizes.add(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(updatedExperiment).length);
        patchSizes.add(FeignUtil.OBJECT_MAPPER.writeValueAsBytes(patch).length);

        return response;
    }
}
