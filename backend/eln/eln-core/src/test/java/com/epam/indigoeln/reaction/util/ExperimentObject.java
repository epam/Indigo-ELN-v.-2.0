package com.epam.indigoeln.reaction.util;

import com.epam.indigoeln.common.model.Paging;
import com.epam.indigoeln.common.util.ModelUtil;
import com.epam.indigoeln.compound.model.search.FindSamplesRequest;
import com.epam.indigoeln.compound.model.search.SampleSearchResult;
import com.epam.indigoeln.compound.model.search.StructuralSearch;
import com.epam.indigoeln.eln.client.CompoundClient;
import com.epam.indigoeln.eln.client.ExperimentClient;
import com.epam.indigoeln.eln.client.MiscClient;
import com.epam.indigoeln.eln.model.ExperimentDetailsDTO;
import com.epam.indigoeln.eln.model.ExperimentStatus;
import com.epam.indigoeln.eln.model.MutationResponse;
import com.epam.indigoeln.reaction.model.*;
import com.epam.indigoeln.reaction.model.mutation.*;
import com.epam.indigoeln.reaction.model.units.*;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static com.epam.indigoeln.compound.model.search.SearchCatalog.ELN;
import static com.google.common.base.Preconditions.checkNotNull;

@Accessors(fluent = true)
public class ExperimentObject {

    @Getter
    private final UUID id;

    @Nullable
    private ExperimentDetailsDTO experiment;

    @Nullable
    private MutationResponse lastMutationResponse;

    private final ExperimentClient experimentClient;
    private final CompoundClient compoundClient;
    private final MiscClient miscClient;

    public ExperimentObject(UUID id, ExperimentClient experimentClient, CompoundClient compoundClient, MiscClient miscClient) {
        this.id = id;
        this.experimentClient = experimentClient;
        this.compoundClient = compoundClient;
        this.miscClient = miscClient;
    }

    public ExperimentObject(ExperimentDetailsDTO experiment, ExperimentClient experimentClient, CompoundClient compoundClient, MiscClient miscClient) {
        this.id = experiment.getId();
        this.experiment = experiment;
        this.experimentClient = experimentClient;
        this.compoundClient = compoundClient;
        this.miscClient = miscClient;
    }

    public MutationResponse mutate(Mutation mutation) {
        boolean undoOrRedo = mutation instanceof ExperimentMutation.Undo || mutation instanceof ExperimentMutation.Redo;
        return mutate(mutation, !undoOrRedo);
    }

    public MutationResponse mutate(Mutation mutation, boolean undoRedo) {
        MutationResponse response = experimentClient.mutateExperimentModel4(id, revision(), mutation);
        lastMutationResponse = response;
        invalidate();
        return response;
    }

    public ExperimentDetailsDTO experiment() {
        if (experiment == null) {
            experiment = experimentClient.getExperiment(id);
        }
        return experiment;
    }

    public ExperimentModel model() {
        return experiment().getModel();
    }

    public Reaction reaction() {
        return getByIndex(model().getReactions(), 1, "Reaction");
    }

    public ReactionInput input(int inputNo) {
        return getByIndex(reaction().getInputs(), inputNo, "Input");
    }

    public ReactionInputSample inputSample(int inputNo, int sampleNo) {
        return getByIndex(input(inputNo).getSamples(), sampleNo, "Sample");
    }

    public ReactionOutput output(int outputNo) {
        return getByIndex(reaction().getOutputs(), outputNo, "Output");
    }

    public ReactionOutputSample outputSample(int outputNo, int sampleNo) {
        return getByIndex(output(outputNo).getSamples(), sampleNo, "Sample");
    }

    public MutationResponse lastMutationResponse() {
        return checkNotNull(lastMutationResponse);
    }

    public String name() {
        return experiment().getName();
    }

    public int revision() {
        return experiment().getRevision();
    }

    public ExperimentStatus status() {
        return experiment().getStatus();
    }

    public void mutateSetSchemeFromResource(String resourceName) {
        String rxnfile = ModelUtil.loadResourceAsString(resourceName);
        mutate(new ReactionMutation.SetScheme(reaction().getAnchor(), rxnfile));
    }

    public void mutateResolveInputs() {
        Map<InputAnchor, UUID> sampleIDs = new HashMap<>();
        Map<InputAnchor, String> requests = experimentClient.analyzeRXN(id, reaction().getAnchor());
        requests.forEach((anchor, structure) -> {
            FindSamplesRequest findSamplesRequest = new FindSamplesRequest().withCatalogs(Set.of(ELN)).withStructure(new StructuralSearch(StructuralSearch.Type.SUBSTRUCTURE, structure));
            SampleSearchResult samples = compoundClient.search(findSamplesRequest, Paging.DEFAULT_PAGE_SIZE);
            if (!samples.items().isEmpty()) {
                sampleIDs.put(anchor, checkNotNull(samples.items().getFirst().getId()));
            }
        });
        mutate(new ReactionMutation.ResolveInputs(reaction().getAnchor(), sampleIDs));
    }

    public void mutateSetInputMol(int inputNo, int sampleNo, @Nullable String mol, @Nullable MolUnit unit) {
        mutate(new ReactionInputSampleMutation.SetInputMol(inputSample(inputNo, sampleNo).getAnchor(), mol, unit));
    }

    public void mutateSetInputWeight(int inputNo, int sampleNo, @Nullable String weight, @Nullable WeightUnit unit) {
        mutate(new ReactionInputSampleMutation.SetInputWeight(inputSample(inputNo, sampleNo).getAnchor(), weight, unit));
    }

    public void mutateSetInputVolume(int inputNo, int sampleNo, @Nullable String volume, @Nullable VolumeUnit unit) {
        mutate(new ReactionInputSampleMutation.SetInputVolume(inputSample(inputNo, sampleNo).getAnchor(), volume, unit));
    }

    public void mutateSetInputDensity(int inputNo, int sampleNo, @Nullable String density, @Nullable DensityUnit unit) {
        mutate(new ReactionInputSampleMutation.SetInputDensity(inputSample(inputNo, sampleNo).getAnchor(), density, unit));
    }

    public void mutateAddEmptyInput() {
        mutate(new ReactionMutation.AddEmptyInput(reaction().getAnchor()));
    }

    public void mutateAddProductSample(int outputNo) {
        mutate(new ReactionOutputMutation.AddProductSample(output(outputNo).getAnchor()));
    }

    public void mutateAddNoProductSample() {
        mutate(new ReactionMutation.AddNoProductSample(reaction().getAnchor()));
    }

    public void update(ExperimentDetailsDTO dto) {
        experiment = dto;
    }

    public void update(Runnable updater) {
        updater.run();
        invalidate();
    }

    public void update(Supplier<ExperimentDetailsDTO> updater) {
        experiment = updater.get();
    }

    public void invalidate() {
        experiment = null;
    }

    @SneakyThrows
    public void generateDetailsReport(File destinationFile) {
        try (Response response = miscClient.generateExperimentDetailsReport(id)) {
            System.err.println("Experiment details report is available at file://" + destinationFile.getAbsolutePath());
            byte[] bytes = (byte[]) response.getEntity();
            Files.write(destinationFile.toPath(), bytes);
        }
    }

    private static <T> T getByIndex(List<T> collection, int index, String collectionName) {
        Preconditions.checkArgument(1 <= index && index <= collection.size(), "%s %s is out of bounds: [1, %s]", collectionName, index, collection.size());
        return collection.get(index - 1);
    }

    public void mutateSetInputRowMol(int inputNo, @Nullable String mol, @Nullable MolUnit unit) {
        mutate(new ReactionInputMutation.SetInputRowMol(input(inputNo).getAnchor(), mol, unit));
    }

    public void mutateSetInputMolarity(int inputNo, int sampleNo, @Nullable String molarity, @Nullable MolarityUnit unit) {
        mutate(new ReactionInputSampleMutation.SetInputMolarity(inputSample(inputNo, sampleNo).getAnchor(), molarity, unit));
    }

    public void mutateSetInputPurity(int inputNo, int sampleNo, @Nullable String purity) {
        mutate(new ReactionInputSampleMutation.SetInputPurity(inputSample(inputNo, sampleNo).getAnchor(), purity));
    }

    public void mutateSetInputRowEQ(int inputNo, @Nullable String eq) {
        mutate(new ReactionInputMutation.SetInputRowEQ(input(inputNo).getAnchor(), eq));
    }

    public void mutateSetInputRowRole(int inputNo, ReactionRole role) {
        mutate(new ReactionInputMutation.SetInputRowRole(input(inputNo).getAnchor(), role));
    }

    public void mutateSetInputRowLimiting(int inputNo) {
        mutate(new ReactionInputMutation.SetInputRowLimiting(input(inputNo).getAnchor()));
    }
}
