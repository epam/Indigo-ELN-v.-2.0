package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.util.JSONPatcher;
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

    @Valid
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, ReactionAnchor.create());
        model.setReactions(List.of(reaction));
        return model;
    }

    public Triple<ExperimentSnapshot, JsonNode, ExperimentMutationContext> applyMutation(ExperimentEntity experiment, ExperimentMutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);
        AbstractExperimentMutationHandler<ExperimentMutation> handler = mutationHandlerRegistry.findHandler(mutation);
        return handler.applyMutation(experiment, mutation);
    }

    @SneakyThrows
    public JsonNode createPatch(ExperimentSnapshot a, ExperimentSnapshot b) {
        JsonNode aJSON = objectMapper.valueToTree(a);
        JsonNode bJSON = objectMapper.valueToTree(b);
        return jsonPatcher.createTopLevel(aJSON, bJSON);
    }

    @SneakyThrows
    public JsonNode rewindSnapshot(ExperimentSnapshot snapshot, List<ExperimentRevisionEntity> revisions) {
        JsonNode json = objectMapper.valueToTree(snapshot);
        for (ExperimentRevisionEntity revision : revisions.reversed()) {
            json = jsonPatcher.reverse(json, revision.getDiff());
        }
        return json;
    }
}
