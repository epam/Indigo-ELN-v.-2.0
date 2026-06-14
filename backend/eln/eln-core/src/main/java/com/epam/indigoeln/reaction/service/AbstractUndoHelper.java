package com.epam.indigoeln.reaction.service;

import com.epam.indigoeln.eln.entity.BaseEntity;
import com.epam.indigoeln.eln.entity.BaseRevisionEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.entity.WithRevision;
import com.epam.indigoeln.eln.util.JSONPatcher;
import com.epam.indigoeln.reaction.model.mutation.Mutation;
import com.epam.indigoeln.reaction.service.mutation.AbstractMutationContext;
import com.epam.indigoeln.reaction.service.mutation.MutationHandler;
import com.epam.indigoeln.reaction.service.mutation.MutationHandlerRegistry;
import com.epam.indigoeln.reaction.service.mutation.MutationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static com.epam.indigoeln.common.exception.InvalidRequestException.fail;
import static com.epam.indigoeln.common.exception.InvalidRequestException.validate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractUndoHelper<E extends BaseEntity & WithRevision, S, R extends BaseRevisionEntity, C extends AbstractMutationContext<E, S, R, C>> {

    private final Class<S> snapshotClass;
    @Inject
    JSONPatcher jsonPatcher;
    @Inject
    MutationHandlerRegistry mutationHandlerRegistry;
    ObjectMapper objectMapper;
    ObjectReader snapshotReader;

    @Inject
    void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        snapshotReader = objectMapper.readerFor(snapshotClass);
    }

    public void doPrepare(E entity, UserEntity user, boolean redo, C context) {
        // TODO load last 1 hour, and either deny undoing beyond that, or load previous data if needed for redo
        List<R> revisions = loadRevisions(entity);
        UndoInfo info = findRevisionToUndoOrRedo(revisions, user, redo);
        validate(info != null, redo ? "Nothing to redo" : "Nothing to undo");
        context.setUndoInfo(info);
    }

    protected abstract List<R> loadRevisions(E entity);

    @SneakyThrows
    public MutationResult doHandle(E entity, S snapshotBefore, C context, boolean redo) {
        // --- not undoable --

        UndoInfo info = checkNotNull(context.getUndoInfo());
        // rewind
        JsonNode snapshotJSON = objectMapper.valueToTree(snapshotBefore);
        for (RevisionInfo revision : info.rewind.reversed()) {
            JsonNode diff = revision.entity.getDiff();
            snapshotJSON = jsonPatcher.reverse(snapshotJSON, diff);
        }
        // store rewound model back in entity
        S snapshot = snapshotReader.readValue(snapshotJSON);
        restoreStateAfterUndo(entity, context, snapshot, info);
        // replay
        try {
            for (RevisionInfo revision : info.replay) {
                revision.handler.doHandle(entity, revision.mutation, context, snapshotBefore);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform " + (redo ? "redo" : "undo") + ": " + e.getMessage(), e);
        }

        afterHandle(info, context, redo);
        return new MutationResult((redo ? "Redo: " : "Undo: ") + info.revision.getRevisionSummary());
    }

    protected void restoreStateAfterUndo(E entity, C context, S snapshot, UndoInfo info) {
        for (RevisionInfo revision : info.rewind.reversed()) {
            revision.handler.doRestoreStateAfterUndo(entity, snapshot, revision.mutation);
        }
    }

    protected void afterHandle(UndoInfo info, C context, boolean redo) {
    }

    @Nullable
    private UndoInfo findRevisionToUndoOrRedo(List<R> revisions, UserEntity user, boolean performRedo) {
        boolean performUndo = !performRedo;
        Map<Integer, RevisionInfo> revisionMap = StreamEx.of(revisions)
                .mapToEntry(BaseRevisionEntity::getRevision, x -> {
                    Mutation mutation = x.getMutation();
                    return new RevisionInfo(x, mutation, mutationHandlerRegistry.findHandler(mutation), x.getUser().getId().equals(user.getId()));
                })
                .toNavigableMap();
        for (RevisionInfo revision : revisionMap.values()) {
            if (revision.entity.getUndoFor() != null) {
                RevisionInfo initial = revisionMap.get(revision.entity.getUndoFor());
                checkState(initial != null);
                revision.undoFor = initial;
                initial.setUndone(true);
            } else if (revision.entity.getRedoFor() != null) {
                RevisionInfo initial = revisionMap.get(revision.entity.getRedoFor());
                checkState(initial != null);
                revision.redoFor = initial;
                initial.setUndone(false);
            } else {
                revision.undone = false; // initial state, not undone
            }
        }

        Deque<RevisionInfo> stack = new ArrayDeque<>();
        for (RevisionInfo revision : revisionMap.values()) {
            if (!revision.madeByCurrentUser) {
                continue;
            }
            if (performUndo) { // build operations stack
                if (revision.undoFor != null) { // undo the last from the stack
                    checkState(!stack.isEmpty() && stack.getLast() == revision.undoFor);
                    stack.removeLast();
                } else {
                    stack.addLast(revision.getInitial());
                }
            } else { // build undo stack
                if (revision.undoFor != null) { // add undo
                    stack.addLast(revision);
                } else if (revision.redoFor != null) { // remove the last undo from the stack
                    if (stack.isEmpty()) {
                        log.debug("Undo stack is empty, nothing to redo");
                        return null;
                    }
                    checkState(stack.getLast().getInitial() == revision.redoFor);
                    stack.removeLast();
                } else { // regular operation, clear undo stack; older operations is unavailable for redo anymore
                    stack.clear();
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Stack for {} for user {}:\n\t{}", performRedo ? "redo" : "undo", user.getUsername(), StreamEx.of(stack)
                    .map(x -> x.entity)
                    .joining("\n\t"));
        }
        if (stack.isEmpty()) {
            return null;
        }
        RevisionInfo found = stack.getLast().getInitial();

        List<RevisionInfo> affectedRevisions = revisionMap.values().stream()
                .filter(x -> x.entity.getRevision() >= found.entity.getRevision())
                .peek(x -> {
                    if (x.isRegular() && !x.handler.isUndoable()) {
                        if (x == found) {
                            fail("Not undoable: " + x.getRevisionSummary());
                        } else {
                            fail("One of affected revisions is not undoable: " + x.getRevisionSummary());
                        }
                    }
                })
                .toList();

        found.setUndone(performUndo);

        return new UndoInfo(
                found,
                // prepare initial revisions corresponding to affected revisions
                affectedRevisions.stream()
                        .map(RevisionInfo::getInitial)
                        .toList(),
                // rewind ALL affected revisions, including undo and redo, to recover exact model state
                affectedRevisions,
                // replay all regular (non-undo/redo) mutations that were not undone
                affectedRevisions.stream()
                        .filter(x -> x.isRegular() && !x.undone)
                        .toList()
        );
    }

    @Getter
    @AllArgsConstructor
    public class UndoInfo {
        private final RevisionInfo revision;
        private final List<RevisionInfo> prepare;
        private final List<RevisionInfo> rewind;
        private final List<RevisionInfo> replay;
    }

    @RequiredArgsConstructor
    public class RevisionInfo {
        private final R entity;
        private final Mutation mutation;
        private final MutationHandler<Mutation, E, S, R, C> handler;
        private final boolean madeByCurrentUser;
        @Nullable
        private RevisionInfo undoFor;
        @Nullable
        private RevisionInfo redoFor;
        private boolean undone;

        public Integer getRevisionNo() {
            return entity.getRevision();
        }

        public String getRevisionSummary() {
            return entity.getSummary();
        }

        private void setUndone(boolean undone) {
            checkState(this.undone != undone);
            this.undone = undone;
        }

        private boolean isRegular() {
            return undoFor == null && redoFor == null;
        }

        private RevisionInfo getInitial() {
            return undoFor != null ? undoFor : redoFor != null ? redoFor : this;
        }
    }
}
