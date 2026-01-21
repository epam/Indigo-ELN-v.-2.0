package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.ProjectEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.ProjectRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.ProjectSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.patch.ProjectPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.ProjectDiffHandler;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.eln.util.ModelUtil.wrapConstraintViolation;

@Slf4j
public abstract class AbstractProjectMutationHandler<T extends Mutation> extends AbstractMutationHandler<T, Void, ProjectEntity, ProjectSnapshot, ProjectPatch> implements ProjectMutationHandler<T> {

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

    @Override
    public Pair<ProjectSnapshot, ProjectPatch> applyMutation(ProjectEntity project, T mutation) {
        return wrapConstraintViolation(
                () -> super.applyMutation(project, mutation),
                e -> mapConstraintToError(e, project)
        );
    }

    @Override
    protected void doValidateAccess(ProjectEntity project, T mutation) {
        aclService.ensureAccess(project, ApplicationPermission.EDIT_PROJECTS);
    }

    @Override
    protected final ProjectSnapshot doSnapshotBefore(ProjectEntity project) {
        return snapshotMapper.createSnapshot(project, isAffectsAttachments(), isAffectsACL());
    }

    @Override
    protected final void doUpdateEntity(ProjectEntity project, @Nullable Void model, ProjectSnapshot snapshotBefore, ProjectSnapshot snapshotAfter) {
        updateDates(project, userService.getCurrentUserEntity());
        //noinspection ConstantValue
        if (project.getId() == null) {
            projectRepository.persist(project);
            projectRepository.flushAndRefresh(project);
        }
    }

    @Override
    protected final ProjectSnapshot doSnapshotAfter(ProjectEntity project, @Nullable Void model) {
        return snapshotMapper.createSnapshot(project, isAffectsAttachments(), isAffectsACL());
    }

    @Override
    protected final ProjectPatch doCreatePatch(ProjectSnapshot snapshotBefore, ProjectSnapshot snapshotAfter) {
        //noinspection DataFlowIssue
        return ProjectDiffHandler.INSTANCE.compare(snapshotBefore, snapshotAfter).updatedValue();
    }

    @Override
    protected final void doCreateRevision(ProjectEntity project, T mutation, MutationResult result, Integer revisionNo, ProjectPatch patch) {
        revisionService.addRevision(project, revisionNo, project.getModifiedAt(), result.summary(), mutation, result.reverseMutation(), patch);
    }

    @Nullable
    private String mapConstraintToError(ConstraintViolationException e, ProjectEntity project) {
        if ("project_name_uq".equals(e.getConstraintName())) {
            return "Project with name '" + project.getName() + "' already exists";
        }
        return null;
    }
}
