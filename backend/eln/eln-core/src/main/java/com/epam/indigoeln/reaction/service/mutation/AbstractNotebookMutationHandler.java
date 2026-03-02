package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.model.patch.NotebookPatch;
import com.epam.indigoeln.reaction.model.patch.handler2.NotebookDiffHandler;
import jakarta.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.eln.util.ModelUtil.wrapConstraintViolation;

public abstract class AbstractNotebookMutationHandler<T extends Mutation> extends AbstractMutationHandler<T, Void, NotebookEntity, NotebookSnapshot, NotebookPatch> implements NotebookMutationHandler<T> {

    @Inject
    protected SnapshotMapper snapshotMapper;
    @Inject
    protected UserService userService;
    @Inject
    protected RevisionService revisionService;
    @Inject
    protected NotebookRepository notebookRepository;
    @Inject
    protected EntityMutationHelper entityMutationHelper;
    @Inject
    protected ACLService aclService;

    @Override
    public Pair<NotebookSnapshot, NotebookPatch> applyMutation(NotebookEntity notebook, T mutation) {
        return wrapConstraintViolation(
                () -> super.applyMutation(notebook, mutation),
                this::mapConstraintToError
        );
    }

    @Override
    protected void doValidateAccess(NotebookEntity notebook, T mutation) {
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
    }

    @Override
    protected final NotebookSnapshot doSnapshotBefore(NotebookEntity notebook) {
        return snapshotMapper.createSnapshot(notebook, isAffectsAttachments(), isAffectsACL());
    }

    @Override
    protected final NotebookPatch doUpdateEntity(NotebookEntity notebook, @Nullable Void model, NotebookSnapshot snapshotBefore, NotebookSnapshot snapshotAfter) {
        updateDates(notebook, userService.getCurrentUserEntity());
        //noinspection ConstantValue
        if (notebook.getId() == null) {
            notebookRepository.persist(notebook);
            notebookRepository.flushAndRefresh(notebook);
        }
        //noinspection DataFlowIssue
        return NotebookDiffHandler.INSTANCE.compare(snapshotBefore, snapshotAfter).updatedValue();
    }

    @Override
    protected final NotebookSnapshot doSnapshotAfter(NotebookEntity notebook, @Nullable Void model) {
        return snapshotMapper.createSnapshot(notebook, isAffectsAttachments(), isAffectsACL());
    }

    @Override
    protected final void doCreateRevision(NotebookEntity notebook, T mutation, MutationResult result, Integer revisionNo, NotebookPatch patch) {
        revisionService.addRevision(notebook, revisionNo, notebook.getModifiedAt(), result.summary(), mutation, result.reverseMutation(), patch);
    }

    @Nullable
    private String mapConstraintToError(ConstraintViolationException e) {
        if ("notebook_name_uq".equals(e.getConstraintName())) {
            return "Unique name is required";
        }
        return null;
    }
}
