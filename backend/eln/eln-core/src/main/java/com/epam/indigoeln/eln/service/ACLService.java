package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static com.epam.indigoeln.eln.model.AccessLevel.*;

@Slf4j
@Transactional
@ApplicationScoped
public class ACLService {

    @Inject
    UserService userService;
    @Inject
    NotebookRepository notebookRepository;
    @Inject
    ExperimentRepository experimentRepository;
    @Inject
    ExperimentService experimentService;

    public void ensureTopLevelAccess(ApplicationPermission operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        throw new AccessDeniedException(operation);
    }

    public void ensureAccess(ProjectEntity project, ApplicationPermission operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        if (!operation.isAllowedBy(project.getCurrentAccess())) {
            throw new AccessDeniedException(ELNEntityType.PROJECT, project.getId(), operation, project.getCurrentAccess());
        }
    }

    public void ensureAccess(NotebookEntity notebook, ApplicationPermission operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        if (!operation.isAllowedBy(notebook.getCurrentAccess())) {
            throw new AccessDeniedException(ELNEntityType.NOTEBOOK, notebook.getId(), operation, notebook.getCurrentAccess());
        }
    }

    public void ensureAccess(ExperimentEntity experiment, ApplicationPermission operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        if (!operation.isAllowedBy(experiment.getCurrentAccess())) {
            throw new AccessDeniedException(ELNEntityType.EXPERIMENT, experiment.getId(), operation, experiment.getCurrentAccess());
        }
    }

    public Set<ApplicationPermission> getCurrentPermissions(@Nullable AccessLevel currentAccess) {
        //noinspection unchecked,rawtypes
        Set<ApplicationPermission> permissions = EnumSet.copyOf((Set) userService.getCurrentUser().getPermissions());
        if (currentAccess != null) {
            permissions.addAll(currentAccess.getGrants());
        }
        return permissions;
    }

    private boolean isUserRolesAllow(ApplicationPermission operation) {
        return userService.getCurrentUser().getPermissions().contains(operation);
    }

    public void initNotebookACL(NotebookEntity notebook) {
        applyImplicitAccess(notebook.getProject(), userService.getCurrentUserEntity(), IMPLICIT_VIEW);
    }

    public void initExperimentACL(ExperimentEntity experiment) {
        if (applyImplicitAccess(experiment.getNotebook(), userService.getCurrentUserEntity(), IMPLICIT_VIEW)) {
            applyImplicitAccess(experiment.getProject(), userService.getCurrentUserEntity(), IMPLICIT_VIEW);
        }
    }

    public void updateProjectACL(ProjectEntity project, List<AccessForm> updates) {
        // preload all ACL lists
        notebookRepository.findByProjectWithACLEntities(project);
        experimentRepository.findByProjectWithACLEntities(project);

        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            applyAccess(project, user, update.getLevel(), update.isDeleteNested());
        }
    }

    public void updateNotebookACL(ProjectEntity project, NotebookEntity notebook, List<AccessForm> updates) {
        // preload all ACL lists
        experimentRepository.findByNotebookWithACLEntities(notebook);

        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(notebook, user, update.getLevel(), update.isDeleteNested())) {
                applyImplicitAccess(project, user, update.getLevel());
            }
        }
    }

    public void updateExperimentACL(ProjectEntity project, NotebookEntity notebook, ExperimentEntity experiment, List<AccessForm> updates) {
        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(experiment, user, update.getLevel(), update.isDeleteNested())) {
                if (applyImplicitAccess(notebook, user, update.getLevel())) {
                    applyImplicitAccess(project, user, update.getLevel());
                }
            }
        }
    }

    private boolean applyImplicitAccess(WithACL<?, ?> container, UserEntity user, AccessLevel childLevel) {
        WithACL<?, ?> containerOrParent = container;
        BaseACLEntity entry = null;
        while (entry == null && containerOrParent != null) {
            entry = containerOrParent.getAclEntities().get(user);
            containerOrParent = containerOrParent.getACLParent();
        }
        if (childLevel != AccessLevel.NONE && entry == null && !container.getCreatedBy().equals(user)) {
            return applyAccess(container, user, IMPLICIT_VIEW, false);
        } else if (childLevel == AccessLevel.NONE && entry != null && entry.getLevel() == IMPLICIT_VIEW) {
            return applyAccess(container, user, AccessLevel.NONE, false);
        }
        return false;
    }

    private boolean applyAccess(WithACL<?, ?> container, UserEntity user, AccessLevel level, boolean deleteNested) {
        if (user.equals(container.getCreatedBy()) && level != AUTHOR) {
            throw new InvalidRequestException("AUTHOR permission cannot be removed from " + user.getUsername());
        }
        if (level == AUTHOR && !user.equals(container.getCreatedBy())) {
            throw new InvalidRequestException("Cannot assign AUTHOR permission to anyone else");
        }
        if (deleteNested && level != NONE) {
            throw new InvalidRequestException("Nested permissions can be deleted only when removing access");
        }
        if (level == AccessLevel.NONE) {
            if (!deleteNested && container.getAclEntities().containsKey(user) && hasNestedAccess(container, user)) {
                // keep the container visible to a user who still has access to some nested entity
                boolean updated = applyAccess(container, user, IMPLICIT_VIEW, false);
                if (container instanceof ProjectEntity project) {
                    for (NotebookEntity notebook : project.getNotebooks()) {
                        if (!notebook.getCreatedBy().equals(user) && !notebook.getAclEntities().containsKey(user) && hasNestedAccess(notebook, user)) {
                            updated |= applyAccess(notebook, user, IMPLICIT_VIEW, false);
                        }
                    }
                }
                return updated;
            }
            boolean removed = container.getAclEntities().remove(user) != null;
            if (removed) {
                log.debug("applyAccess: removed {} from {}", user, container);
            }
            if (deleteNested) {
                if (container instanceof ProjectEntity project) {
                    for (NotebookEntity notebook : project.getNotebooks()) {
                        removed |= applyAccess(notebook, user, AccessLevel.NONE, true);
                    }
                } else if (container instanceof NotebookEntity notebook) {
                    for (ExperimentEntity experiment : notebook.getExperiments()) {
                        removed |= applyAccess(experiment, user, AccessLevel.NONE, true);
                    }
                }
            }
            return removed;
        }
        BaseACLEntity entry = container.getAclEntities().get(user);
        if (entry == null) {
            container.insertACL(user, level);
            log.debug("applyAccess: inserted {} for {} to {}", level, user, container);
            return true;
        }
        if (entry.getLevel() != level) {
            log.debug("applyAccess: updated {}->{}, for {} in {}", entry.getLevel(), level, user, container);
            entry.setLevel(level);
            return true;
        }
        return false;
    }

    private boolean hasNestedAccess(WithACL<?, ?> container, UserEntity user) {
        Set<? extends WithACL<?, ?>> nested = switch (container) {
            case ProjectEntity p -> p.getNotebooks();
            case NotebookEntity n -> n.getExperiments();
            default -> Set.of();
        };
        return nested.stream().anyMatch(child -> child.getCreatedBy().equals(user)
                || child.getAclEntities().containsKey(user)
                || hasNestedAccess(child, user));
    }
}
