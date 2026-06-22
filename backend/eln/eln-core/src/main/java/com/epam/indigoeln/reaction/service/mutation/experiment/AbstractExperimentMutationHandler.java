package com.epam.indigoeln.reaction.service.mutation.experiment;

import com.epam.indigoeln.eln.entity.ExperimentEntity;
import com.epam.indigoeln.eln.entity.ExperimentRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.indigowrapper.IndigoAPI;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.model.ExperimentSnapshot;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelHelperService;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import com.epam.indigoeln.reaction.service.calculator.ReactionCalculator;
import com.epam.indigoeln.reaction.service.mutation.ExperimentModelMutationListener;
import com.epam.indigoeln.reaction.service.mutation.MutationHandler;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.arc.All;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;

import java.util.List;
import java.util.Set;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.reaction.util.SignificantFiguresUtil.runWithSignificantFigures;

@Slf4j
public abstract class AbstractExperimentMutationHandler<T extends ExperimentMutation> extends MutationHandler<T, ExperimentEntity, ExperimentSnapshot, ExperimentRevisionEntity, ExperimentMutationContext> {

    @Inject
    SnapshotMapper snapshotMapper;
    @Inject
    ExperimentModelService experimentModelService;
    @Inject
    ExperimentModelHelperService experimentModelHelperService;
    @Inject
    Instance<ReactionCalculator> reactionCalculatorFactory;
    @Inject
    IndigoAPI indigoAPI;
    @Inject
    Validator validator;
    @Inject
    UserService userService;
    @Inject
    RevisionService revisionService;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    protected ACLService aclService;

    @All
    @Inject
    List<ExperimentModelMutationListener> listeners;

    @Override
    protected ExperimentMutationContext createContext() {
        return new ExperimentMutationContext();
    }

    @Override
    protected void doValidateAccess(ExperimentEntity experiment, T mutation, ExperimentMutationContext context) {
        aclService.ensureAccess(experiment, ApplicationPermission.EDIT_EXPERIMENTS);
    }

    protected final ExperimentSnapshot doSnapshotBefore(ExperimentEntity experiment, ExperimentMutationContext context) {
        return snapshotMapper.createSnapshot(experiment, true);
    }

    @Override
    protected final JsonNode doUpdateEntity(ExperimentEntity experiment, ExperimentSnapshot snapshotBefore, ExperimentSnapshot snapshotAfter, ExperimentMutationContext context) {
        return runWithSignificantFigures(experiment.getModel().getSignificantFigures(), () -> {
            JsonNode patch;
            doNotifyBeforeRecalculate(experiment, context);
            ReactionCalculator calculator = reactionCalculatorFactory.get();
            try {
                calculator.recalculate(experiment.getModel());
                doNotifyAfterRecalculate(experiment, context);
                doValidateModel(experiment.getModel());
                updateDates(experiment, userService.getCurrentUserEntity());
                patch = experimentModelService.createPatch(snapshotBefore, snapshotAfter);
                calculator.cleanupOverwritten();
            } finally {
                reactionCalculatorFactory.destroy(calculator);
            }
            //noinspection ConstantValue
            if (experiment.getId() == null) {
                experimentRepository.persist(experiment);
                experimentRepository.flushAndRefresh(experiment);
            }
            return patch;
        });
    }

    @Override
    protected ExperimentSnapshot doSnapshotAfter(ExperimentEntity experiment, ExperimentMutationContext context) {
        return snapshotMapper.createSnapshot(experiment, false);
    }

    @Override
    protected ExperimentRevisionEntity doCreateRevision(ExperimentEntity experiment, T mutation, String summary, Integer revisionNo, JsonNode patch, ExperimentMutationContext context, ExperimentSnapshot snapshotAfter) {
        ExperimentRevisionEntity revision = revisionService.addRevision(experiment, revisionNo, experiment.getModifiedAt(), summary, mutation, patch);
        if (!context.getResponse().getMessages().isEmpty()) {
            revision.setMessages(context.getResponse().getMessages().toArray(new String[0]));
        }
        return revision;
    }

    protected void doValidateModel(ExperimentModel model) {
        Set<ConstraintViolation<ExperimentModel>> violations = validator.validate(model);
        if (!violations.isEmpty()) {
            log.error("Mutation produced invalid model:\n{}", StreamEx.of(violations).joining("\n"));
            throw new RuntimeException("Mutation produced invalid model:\n" + StreamEx.of(violations).joining("\n"));
        }
    }

    @Override
    protected void doNotifyBeforeHandle(ExperimentEntity entity, T mutation, ExperimentMutationContext context) {
        for (ExperimentModelMutationListener listener : listeners) {
            listener.beforeHandle(entity, context);
        }
    }

    private void doNotifyBeforeRecalculate(ExperimentEntity entity, ExperimentMutationContext context) {
        for (ExperimentModelMutationListener listener : listeners) {
            listener.beforeRecalculate(entity, context);
        }
    }

    private void doNotifyAfterRecalculate(ExperimentEntity entity, ExperimentMutationContext context) {
        for (ExperimentModelMutationListener listener : listeners) {
            listener.afterRecalculate(entity, context);
        }
    }
}
