package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.mutation.MutationContext;
import com.epam.indigoeln.reaction.model.patch.ExperimentPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ExperimentDiffHandler;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.ExperimentMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Transactional
@ApplicationScoped
public class ExperimentModelService {

    @Inject
    ReactionCalculator reactionCalculator;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;
    @Inject
    ExperimentModelHelperService experimentModelHelperService;
    @Inject
    IndigoAPI indigoAPI;
    @Inject
    UserService userService;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    Validator validator;
    @Inject
    RevisionService revisionService;

    ObjectReader modelReader;
    ObjectWriter modelWriter;
    ObjectReader patchReader;
    ObjectWriter patchWriter;

    ExperimentModelService(ObjectMapper objectMapper) {
        modelReader = objectMapper.readerFor(ExperimentModel.class);
        modelWriter = objectMapper.writerFor(ExperimentModel.class);
        patchReader = objectMapper.readerFor(ExperimentPatch.class);
        patchWriter = objectMapper.writerFor(ExperimentPatch.class);
    }

    @Valid
    public ExperimentModel createNewModel(ExperimentEntity experiment) {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, experiment.generateNextAnchor(ReactionAnchor.class));
        model.setReactions(List.of(reaction));
        model.setSchemaVersion(ExperimentModel.SCHEMA_VERSION);
        return model;
    }

    public Pair<ExperimentSnapshot, ExperimentPatch> applyMutation(ExperimentEntity experiment, Mutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);
        ExperimentMutationHandler<Mutation, ?> handler = mutationHandlerRegistry.findHandler(mutation);
        return handler.applyMutation(experiment, mutation);
    }

    public ExperimentPatch createPatch(ExperimentSnapshot a, ExperimentSnapshot b, MutationContext context) {
        ExperimentDiffHandler valueHandler = new ExperimentDiffHandler(context);
        //noinspection DataFlowIssue
        return valueHandler.compare(a, b).updatedValue();
    }

    public ExperimentModel getModel(ExperimentEntity experiment) {
        try {
            return modelReader.readValue(experiment.getModel());
        } catch (Exception e) {
            throw new RuntimeException("Cannot read experiment model: " + e.getMessage(), e);
        }
    }

    public void setModel(ExperimentEntity experiment, ExperimentModel model) {
        try {
            experiment.setModel(modelWriter.writeValueAsString(model));
        } catch (Exception e) {
            throw new RuntimeException("Cannot write experiment model: " + e.getMessage(), e);
        }
    }
}
