package com.epam.indigoeln.reaction.service.mutation.notebook;

import com.epam.indigoeln.eln.entity.NotebookEntity;
import com.epam.indigoeln.eln.entity.NotebookRevisionEntity;
import com.epam.indigoeln.eln.mapper.SnapshotMapper;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.eln.service.ACLService;
import com.epam.indigoeln.eln.service.NotebookService;
import com.epam.indigoeln.eln.service.RevisionService;
import com.epam.indigoeln.eln.service.UserService;
import com.epam.indigoeln.eln.util.JSONPatcher;
import com.epam.indigoeln.reaction.model.NotebookSnapshot;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.mutation.EntityMutationHelper;
import com.epam.indigoeln.reaction.service.mutation.MutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.epam.indigoeln.reaction.service.mutation.NotebookMutationListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.All;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.epam.indigoeln.eln.util.ModelUtil.updateDates;
import static com.epam.indigoeln.eln.util.ModelUtil.wrapConstraintViolation;

public abstract class AbstractNotebookMutationHandler<T extends Mutation> extends MutationHandler<T, NotebookEntity, NotebookSnapshot, NotebookRevisionEntity, NotebookMutationContext, NotebookMutationListener> {

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
    @Inject
    ObjectMapper objectMapper;
    @Inject
    JSONPatcher jsonPatcher;
    @Inject
    NotebookService notebookService;

    @All
    @Inject
    @Getter(AccessLevel.PROTECTED)
    List<NotebookMutationListener> listeners;

    @Override
    protected NotebookMutationContext createContext() {
        return new NotebookMutationContext();
    }

    @Override
    public MutationResult<NotebookSnapshot, NotebookMutationContext> applyMutation(NotebookEntity notebook, T mutation) {
        return wrapConstraintViolation(
                () -> super.applyMutation(notebook, mutation),
                this::mapConstraintToError
        );
    }

    @Override
    protected void doValidateAccess(NotebookEntity notebook) {
        aclService.ensureAccess(notebook, ApplicationPermission.EDIT_NOTEBOOKS);
    }

    @Override
    protected void doValidateStatus(NotebookEntity entity) {
        // nothing
    }

    @Override
    protected final NotebookSnapshot doSnapshotBefore(NotebookEntity notebook, NotebookMutationContext context) {
        return snapshotMapper.createSnapshot(notebook);
    }

    @Override
    @SneakyThrows
    protected final JsonNode doUpdateEntity(NotebookEntity notebook, NotebookSnapshot snapshotBefore, NotebookSnapshot snapshotAfter, NotebookMutationContext context) {
        updateDates(notebook, userService.getCurrentUserEntity());
        //noinspection ConstantValue
        if (notebook.getId() == null) {
            notebookRepository.persist(notebook);
            notebookRepository.flushAndRefresh(notebook);
        }
        JsonNode beforeJSON = objectMapper.valueToTree(snapshotBefore);
        JsonNode afterJSON = objectMapper.valueToTree(snapshotAfter);
        return jsonPatcher.createTopLevel(beforeJSON, afterJSON);
    }

    @Override
    protected final NotebookSnapshot doSnapshotAfter(NotebookEntity notebook, NotebookMutationContext context) {
        return snapshotMapper.createSnapshot(notebook);
    }

    @Override
    protected final NotebookRevisionEntity doCreateRevision(NotebookEntity notebook, T mutation, String summary, Integer revisionNo, JsonNode patch, NotebookMutationContext context, NotebookSnapshot snapshotAfter) {
        return revisionService.addRevision(notebook, revisionNo, notebook.getModifiedAt(), summary, mutation, patch);
    }

    @Nullable
    private String mapConstraintToError(ConstraintViolationException e) {
        if ("notebook_name_uq".equals(e.getConstraintName())) {
            return "Unique name is required";
        }
        return null;
    }
}
