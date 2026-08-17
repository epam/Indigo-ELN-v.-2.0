package com.epam.indigoeln.reaction.service.mutation.project;

import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.entity.ProjectRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.eln.util.JSONPatcher;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.ProjectMutationListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.All;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.eln.util.ModelUtil.wrapConstraintViolation;

@Slf4j
public abstract class AbstractProjectMutationHandler<T extends Mutation> extends MutationHandler<T, ProjectEntity, ProjectSnapshot, ProjectRevisionEntity, ProjectMutationContext, ProjectMutationListener> {

    @Inject
    protected SnapshotMapper snapshotMapper;
    @Inject
    protected UserService userService;
    @Inject
    protected RevisionService revisionService;
    @Inject
    protected ProjectRepository projectRepository;
    @Inject
    protected EntityMutationHelper entityMutationHelper;
    @Inject
    protected ACLService aclService;
    @Inject
    protected ObjectMapper objectMapper;
    @Inject
    protected JSONPatcher jsonPatcher;

    @All
    @Inject
    @Getter(AccessLevel.PROTECTED)
    List<ProjectMutationListener> listeners;

    @Override
    protected ProjectMutationContext createContext() {
        return new  ProjectMutationContext();
    }

    @Override
    public MutationResult<ProjectSnapshot, ProjectMutationContext> applyMutation(ProjectEntity project, T mutation) {
        return wrapConstraintViolation(
                () -> super.applyMutation(project, mutation),
                this::mapConstraintToError
        );
    }

    @Override
    protected void doValidateAccess(ProjectEntity project) {
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
    }

    @Override
    protected void doValidateStatus(ProjectEntity entity) {
        // nothing
    }

    @Override
    protected final ProjectSnapshot doSnapshotBefore(ProjectEntity project, ProjectMutationContext context) {
        return snapshotMapper.createSnapshot(project);
    }

    @Override
    @SneakyThrows
    protected final JsonNode doUpdateEntity(ProjectEntity project, ProjectSnapshot snapshotBefore, ProjectSnapshot snapshotAfter, ProjectMutationContext context) {
        updateDates(project, userService.getCurrentUserEntity());
        //noinspection ConstantValue
        if (project.getId() == null) {
            projectRepository.persist(project);
            projectRepository.flushAndRefresh(project);
        }
        JsonNode beforeJSON = objectMapper.valueToTree(snapshotBefore);
        JsonNode afterJSON = objectMapper.valueToTree(snapshotAfter);
        return jsonPatcher.createTopLevel(beforeJSON, afterJSON);
    }

    @Override
    protected final ProjectSnapshot doSnapshotAfter(ProjectEntity project, ProjectMutationContext context) {
        return snapshotMapper.createSnapshot(project);
    }

    @Override
    protected final ProjectRevisionEntity doCreateRevision(ProjectEntity project, T mutation, String summary, Integer revisionNo, JsonNode patch, ProjectMutationContext context, ProjectSnapshot snapshotAfter) {
        return revisionService.addRevision(project, revisionNo, project.getModifiedAt(), summary, mutation, patch);
    }

    @Nullable
    private String mapConstraintToError(ConstraintViolationException e) {
        if ("project_name_uq".equals(e.getConstraintName())) {
            return "Unique name is required";
        }
        return null;
    }
}
