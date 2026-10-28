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
        updateImplicitAccess(notebook, userService.getCurrentUserEntity(), true);
    }

    public void initExperimentACL(ExperimentEntity experiment) {
        updateImplicitAccess(experiment, userService.getCurrentUserEntity(), true);
    }

    public void updateProjectACL(ProjectEntity project, List<AccessForm> updates) {
        // preload all ACL lists
        notebookRepository.findByProjectWithACLEntities(project);
        experimentRepository.findByProjectWithACLEntities(project);

        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(project, user, update.getLevel(), update.isDeleteNested()) && update.isDeleteNested()) {
                for (NotebookEntity notebook : project.getNotebooks()) {
                    refreshImplicitAccess(notebook, user);
                }
                refreshImplicitAccess(project, user);
            }
        }
    }

    public void updateNotebookACL(NotebookEntity notebook, List<AccessForm> updates) {
        // preload all ACL lists
        experimentRepository.findByNotebookWithACLEntities(notebook);

        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(notebook, user, update.getLevel(), update.isDeleteNested())) {
                if (update.isDeleteNested()) {
                    refreshImplicitAccess(notebook, user);
                }
                updateImplicitAccess(notebook, user, update.getLevel() != NONE);
            }
        }
    }

    public void updateExperimentACL(ExperimentEntity experiment, List<AccessForm> updates) {
        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(experiment, user, update.getLevel(), update.isDeleteNested())) {
                updateImplicitAccess(experiment, user, update.getLevel() != NONE);
            }
        }
    }

    /**
     * Brings implicit access of all containers above {@code entity} in line with the user's access to nested entities.
     * {@code granted} means the user has just gained access to {@code entity}, so no need to look further.
     */
    private void updateImplicitAccess(WithACL<?, ?> entity, UserEntity user, boolean granted) {
        for (WithACL<?, ?> container = entity.getACLParent(); container != null; container = container.getACLParent()) {
            if (granted) {
                container.getImplicitUsers().add(user);
            } else {
                refreshImplicitAccess(container, user);
            }
        }
    }

    private void refreshImplicitAccess(WithACL<?, ?> container, UserEntity user) {
        if (hasNestedAccess(container, user)) {
            container.getImplicitUsers().add(user);
        } else {
            container.getImplicitUsers().remove(user);
        }
    }

    private boolean applyAccess(WithACL<?, ?> container, UserEntity user, AccessLevel level, boolean deleteNested) {
        if (user.equals(container.getCreatedBy()) && level != AUTHOR) {
            throw new InvalidRequestException("AUTHOR permission cannot be removed from " + user.getUsername());
        }
        if (level == AUTHOR && !user.equals(container.getCreatedBy())) {
            throw new InvalidRequestException("Cannot assign AUTHOR permission to anyone else");
        }
        if (level == IMPLICIT_VIEW) {
            throw new InvalidRequestException("IMPLICIT_VIEW permission cannot be assigned");
        }
        if (deleteNested && level != NONE) {
            throw new InvalidRequestException("Nested permissions can be deleted only when removing access");
        }
        if (level == AccessLevel.NONE) {
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
