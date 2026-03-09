package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ExperimentEditSessionEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
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
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

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
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, new ReactionAnchor(UUID.randomUUID()));
        model.setReactions(List.of(reaction));
        model.setSchemaVersion(ExperimentModel.SCHEMA_VERSION);
        model.setSignificantFigures(ExperimentModel.DEFAULT_SIGNIFICANT_FIGURES);
        return model;
    }

    public Pair<ExperimentSnapshot, ExperimentPatch> applyMutation(ExperimentEntity experiment, Mutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);
        ExperimentMutationHandler<Mutation> handler = mutationHandlerRegistry.findHandler(mutation);
        return handler.applyMutation(experiment, mutation);
    }

    public ExperimentPatch createPatch(ExperimentSnapshot a, ExperimentSnapshot b) {
        ExperimentDiffHandler valueHandler = ExperimentDiffHandler.INSTANCE;
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

    @Nullable
    public ExperimentEditSessionEntity getEditSession(ExperimentEntity experiment, UserEntity user) {
        ExperimentEditSessionEntity session = experimentRepository.findActiveEditSession(experiment, user);
        if (session != null) {
            session = closeEditSessionIfInactive(session);
        }
        return session;
    }

    public ExperimentEditSessionEntity createEditSession(ExperimentEntity experiment, UserEntity user, ZonedDateTime dateTime) {
        ExperimentEditSessionEntity session = new ExperimentEditSessionEntity();
        session.setExperiment(experiment);
        session.setUser(user);
        session.setStarted(dateTime);
        session.setLastActive(dateTime);
        experiment.getEditSessions().add(session);
        experimentRepository.persistEditSession(session);
        return session;
    }

    @Nullable
    private ExperimentEditSessionEntity closeEditSessionIfInactive(ExperimentEditSessionEntity editSession) {
        Duration durationSinceLastActive = Duration.between(editSession.getLastActive(), ZonedDateTime.now());
        if (durationSinceLastActive.compareTo(Duration.ofHours(1)) > 0) {
            editSession.setFinished(editSession.getLastActive());
            return null;
        }
        return editSession;
    }
}
