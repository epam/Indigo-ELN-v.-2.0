package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.ExperimentEditSessionEntity;
import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.util.JSONPatcher;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.Reaction;
import com.epam.indigoeln.reaction.model.ReactionAnchor;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.experiment.AbstractExperimentMutationHandler;
import com.epam.indigoeln.reaction.service.mutation.experiment.ExperimentMutationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

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

    private final ObjectMapper objectMapper;
    private final ObjectReader modelReader;
    private final ObjectWriter modelWriter;

    ExperimentModelService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        modelReader = objectMapper.readerFor(ExperimentModel.class);
        modelWriter = objectMapper.writerFor(ExperimentModel.class);
    }

    @Valid
    public ExperimentModel createNewModel() {
        ExperimentModel model = new ExperimentModel();
        Reaction reaction = Reaction.create(model, new ReactionAnchor(UUID.randomUUID()));
        model.setReactions(List.of(reaction));
        model.setSignificantFigures(ExperimentModel.DEFAULT_SIGNIFICANT_FIGURES);
        return model;
    }

    public Triple<ExperimentSnapshot, JsonNode, ExperimentMutationContext> applyMutation(ExperimentEntity experiment, Mutation mutation) {
        log.debug("Mutating experiment {}: {}", experiment.getId(), mutation);
        AbstractExperimentMutationHandler<Mutation> handler = mutationHandlerRegistry.findHandler(mutation);
        return handler.applyMutation(experiment, mutation);
    }

    @SneakyThrows
    public JsonNode createPatch(ExperimentSnapshot a, ExperimentSnapshot b) {
        JsonNode aJSON = objectMapper.valueToTree(a);
        JsonNode bJSON = objectMapper.valueToTree(b);
        return jsonPatcher.createTopLevel(aJSON, bJSON);
    }

    public void readModel(ExperimentEntity experiment) {
        experiment.setModelObj(getModel(experiment));
    }

    public void writeModel(ExperimentEntity experiment) {
        setModel(experiment, checkNotNull(experiment.getModelObj()));
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
