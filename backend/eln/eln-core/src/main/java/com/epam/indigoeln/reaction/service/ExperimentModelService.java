package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.util.JSONPatcher;
import com.epam.indigoeln.eln.util.PatchFormatter;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.experiment.AbstractExperimentMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

@Slf4j
@Transactional
@ApplicationScoped
public class ExperimentModelService {

    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    JSONPatcher jsonPatcher;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    Instance<PatchFormatter> patchFormatterInstance;

    @Valid
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, ReactionAnchor.create());
        model.setReactions(List.of(reaction));
        return model;
    }

    public Triple<ExperimentSnapshot, JsonNode, ExperimentMutationContext> applyMutation(ExperimentEntity experiment, ExperimentMutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);
        return mutationHandlerRegistry.withHandler(mutation, (AbstractExperimentMutationHandler<ExperimentMutation> handler) -> handler.applyMutation(experiment, mutation));
    }

    @SneakyThrows
    public JsonNode createPatch(ExperimentSnapshot a, ExperimentSnapshot b) {
        JsonNode aJSON = objectMapper.valueToTree(a);
        JsonNode bJSON = objectMapper.valueToTree(b);
        JsonNode patch = jsonPatcher.createTopLevel(aJSON, bJSON);
        // TODO remove after testing
        JsonNode applied = jsonPatcher.apply(aJSON, patch);
        ExperimentSnapshot restoredB = objectMapper.treeToValue(applied, ExperimentSnapshot.class);
        restoredB.setRevision(b.getRevision()); // diff doesn't contain revision
        if (!restoredB.equals(b)) {
            restoredB.getAttachments().iterator().next().getCreatedAt().equals(b.getAttachments().iterator().next().getCreatedAt());
            throw new IllegalStateException("Diff calculated incorrectly:\nBefore: %s\nDiff: %s\nAfter: %s\nRestored: %s\n".formatted(
                    objectMapper.writeValueAsString(a), objectMapper.writeValueAsString(patch), objectMapper.writeValueAsString(b), objectMapper.writeValueAsString(restoredB)
            ));
        }
        applied = jsonPatcher.reverse(bJSON, patch);
        ExperimentSnapshot restoredA = objectMapper.treeToValue(applied, ExperimentSnapshot.class);
        restoredA.setRevision(a.getRevision());
        if (!restoredA.equals(a)) {
            throw new IllegalStateException("Reverse diff calculated incorrectly:\nBefore: %s\nDiff: %s\nAfter: %s\nRestored: %s\n".formatted(
                    objectMapper.writeValueAsString(a), objectMapper.writeValueAsString(patch), objectMapper.writeValueAsString(b), objectMapper.writeValueAsString(restoredA)
            ));
        }
        return patch;
    }

    @SneakyThrows
    public JsonNode rewindSnapshot(ExperimentSnapshot snapshot, List<ExperimentRevisionEntity> revisions) {
        JsonNode json = objectMapper.valueToTree(snapshot);
        for (ExperimentRevisionEntity revision : revisions.reversed()) {
            json = jsonPatcher.reverse(json, revision.getDiff());
        }
        return json;
    }

    public String formatDiff(JsonNode before, ExperimentRevisionEntity targetRevision) {
        PatchFormatter formatter = patchFormatterInstance.get();
        try {
            return formatter.format(before, targetRevision.getDiff());
        } finally {
            patchFormatterInstance.destroy(formatter);
        }
    }
}
