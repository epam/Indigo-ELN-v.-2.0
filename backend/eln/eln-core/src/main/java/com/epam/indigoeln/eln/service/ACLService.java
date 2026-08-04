package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.ApplicationPermission;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import com.epam.indigoeln.reaction.model.mutation.ExperimentMutation;
import com.epam.indigoeln.reaction.model.mutation.NotebookMutation;
import com.epam.indigoeln.reaction.model.mutation.ProjectMutation;
import com.epam.indigoeln.reaction.service.ExperimentModelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

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
    NotebookService notebookService;
    @Inject
    ProjectService projectService;
    @Inject
    ExperimentService experimentService;
    @Inject
    ExperimentModelService experimentModelService;

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
        AccessLevel currentAccess = project.getCalculatedInfo() != null ? project.getCalculatedInfo().getCurrentAccess() : NONE;
        if (!operation.isAllowedBy(currentAccess)) {
            throw new AccessDeniedException(ELNEntityType.PROJECT, project.getId(), operation, currentAccess);
        }
    }

    public void ensureAccess(NotebookEntity notebook, ApplicationPermission operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        AccessLevel currentAccess = notebook.getCalculatedInfo() != null ? notebook.getCalculatedInfo().getCurrentAccess() : NONE;
        if (!operation.isAllowedBy(currentAccess)) {
            throw new AccessDeniedException(ELNEntityType.NOTEBOOK, notebook.getId(), operation, currentAccess);
        }
    }

    public void ensureAccess(ExperimentEntity experiment, ApplicationPermission operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        AccessLevel currentAccess = experiment.getCalculatedInfo() != null ? experiment.getCalculatedInfo().getCurrentAccess() : NONE;
        if (!operation.isAllowedBy(currentAccess)) {
            throw new AccessDeniedException(ELNEntityType.EXPERIMENT, experiment.getId(), operation, currentAccess);
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

    public void initProjectACL(ProjectEntity project) {
        recalculateACL(project);
    }

    public void initNotebookACL(NotebookEntity notebook) {
        ProjectEntity project = notebook.getProject();
        recalculateACL(notebook);
        if (applyImplicitAccess(project, userService.getCurrentUserEntity(), IMPLICIT_VIEW, () -> true)) {
            projectService.applyMutation(project, new ProjectMutation.ProjectAccessUpdated(notebook.getName(), null));
        }
    }

    public void initExperimentACL(ExperimentEntity experiment) {
        ProjectEntity project = experiment.getProject();
        NotebookEntity notebook = experiment.getNotebook();
        recalculateACL(experiment);
        if (applyImplicitAccess(notebook, userService.getCurrentUserEntity(), IMPLICIT_VIEW, () -> true)) {
            notebookService.applyMutation(notebook, new NotebookMutation.NotebookAccessUpdated(null, experiment.getName()));
            if (applyImplicitAccess(project, userService.getCurrentUserEntity(), IMPLICIT_VIEW, () -> true)) {
                projectService.applyMutation(project, new ProjectMutation.ProjectAccessUpdated(notebook.getName(), null));
            }
        }
    }

    public void updateProjectACL(ProjectEntity project, List<AccessForm> updates) {
        // preload all ACL lists
        notebookRepository.findByProjectWithACLEntities(project);
        experimentRepository.findByProjectWithACLEntities(project);

        boolean updated = false;
        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(project, user, update.getLevel(), update.isDeleteNested())) {
                updated = true;
            }
        }

        if (updated) {
            recalculateACL(project);
            for (NotebookEntity notebook : project.getNotebooks()) {
                notebookService.applyMutation(notebook, new NotebookMutation.NotebookAccessUpdated(project.getName(), null));
                for (ExperimentEntity experiment : notebook.getExperiments()) {
                    experimentModelService.applyMutation(experiment, new ExperimentMutation.ExperimentAccessUpdated(project.getName(), null));
                }
            }
        }
    }

    public void updateNotebookACL(ProjectEntity project, NotebookEntity notebook, List<AccessForm> updates) {
        // preload all ACL lists
        experimentRepository.findByNotebookWithACLEntities(notebook);

        boolean updated = false;
        boolean updatedImplicitViewProject = false;
        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(notebook, user, update.getLevel(), update.isDeleteNested())) {
                updated = true;
                updatedImplicitViewProject |= applyImplicitAccess(project, user, update.getLevel(), () -> notebookRepository.hasAccessibleNotebooks(project));
            }
        }

        if (updated) {
            recalculateACL(notebook);
            for (ExperimentEntity experiment : notebook.getExperiments()) {
                experimentModelService.applyMutation(experiment, new ExperimentMutation.ExperimentAccessUpdated(null, notebook.getName()));
            }
        }
        if (updatedImplicitViewProject) {
            projectService.applyMutation(project, new ProjectMutation.ProjectAccessUpdated(notebook.getName(), null));
        }
    }

    public void updateExperimentACL(ProjectEntity project, NotebookEntity notebook, ExperimentEntity experiment, List<AccessForm> updates) {
        boolean updated = false;
        boolean updatedImplicitViewNotebook = false;
        boolean updatedImplicitViewProject = false;
        for (AccessForm update : updates) {
            UserEntity user = userService.getEntity(update.getUsername());
            if (applyAccess(experiment, user, update.getLevel(), update.isDeleteNested())) {
                updated = true;
                if (applyImplicitAccess(notebook, user, update.getLevel(), () -> experimentRepository.hasAccessibleExperiments(notebook))) {
                    updatedImplicitViewNotebook = true;
                    if (applyImplicitAccess(project, user, update.getLevel(), () -> notebookRepository.hasAccessibleNotebooks(project))) {
                        updatedImplicitViewProject = true;
                    }
                }
            }
        }

        if (updated) {
            recalculateACL(experiment);
        }
        if (updatedImplicitViewNotebook) {
            notebookService.applyMutation(notebook, new NotebookMutation.NotebookAccessUpdated(null, experiment.getName()));
        }
        if (updatedImplicitViewProject) {
            projectService.applyMutation(project, new ProjectMutation.ProjectAccessUpdated(null, experiment.getName()));
        }
    }

    private boolean applyImplicitAccess(WithACL<?, ?> container, UserEntity user, AccessLevel childLevel, Supplier<Boolean> isAccessible) {
        WithACL<?, ?> containerOrParent = container;
        BaseACLEntity entry = null;
        while (entry == null && containerOrParent != null) {
            entry = containerOrParent.getAclEntities().get(user);
            containerOrParent = containerOrParent.getACLParent();
        }
        if (childLevel != AccessLevel.NONE && entry == null && !container.getCreatedBy().equals(user)) {
            return applyAccess(container, user, IMPLICIT_VIEW, false);
        } else if (childLevel == AccessLevel.NONE && entry != null && entry.getLevel() == IMPLICIT_VIEW && !isAccessible.get()) {
            return applyAccess(container, user, AccessLevel.NONE, false);
        }
        return false;
    }

    public void recalculateACL(WithACL<?, ?> child) {
        log.debug("recalculateACL: {}", child);
        Map<UserEntity, Pair<AccessLevel, Boolean>> users = new HashMap<>();
        users.put(child.getCreatedBy(), Pair.of(AUTHOR, false));
        log.debug("recalculateACL: author {}", child.getCreatedBy());
        List<Map<UserEntity, ? extends BaseACLEntity>> inheritedACLs = switch (child) {
            case ProjectEntity p -> List.of();
            case NotebookEntity n -> List.of(n.getProject().getAclEntities());
            case ExperimentEntity e -> List.of(e.getNotebook().getAclEntities(), e.getProject().getAclEntities());
            default -> throw new IllegalStateException("Unexpected: " + child);
        };
        Set<? extends WithACL<?, ?>> nestedACLs = switch (child) {
            case ProjectEntity p -> p.getNotebooks();
            case NotebookEntity n -> n.getExperiments();
            case ExperimentEntity e -> Set.of();
            default -> throw new IllegalStateException("Unexpected: " + child);
        };
        child.getAclEntities().forEach((user, level) -> {
            if (level.getLevel() != IMPLICIT_VIEW && users.putIfAbsent(user, Pair.of(level.getLevel(), false)) == null) {
                log.debug("recalculateACL: added {}={} from own ACL", user, level.getLevel());
            }
        });
        for (Map<UserEntity, ? extends BaseACLEntity> acl : inheritedACLs) {
            acl.forEach((user, level) -> {
                if (level.getLevel() != IMPLICIT_VIEW && users.putIfAbsent(user, Pair.of(level.getLevel(), true)) == null) {
                    log.debug("recalculateACL: added {}={} from one of parent entities", user, level.getLevel());
                }
            });
        }
        for (WithACL<?, ?> nested : nestedACLs) {
            nested.getAclEntities().forEach((user, level) -> {
                if (users.putIfAbsent(user, Pair.of(IMPLICIT_VIEW, false)) == null) {
                    log.debug("recalculateACL: added {}=IMPLICIT_VIEW from one of child entities", user);
                }
            });
        }
        Map<UserEntity, Pair<AccessLevel, Boolean>> sortedUsers = EntryStream.of(users)
                        .sorted(Comparator.<Map.Entry<UserEntity, Pair<AccessLevel, Boolean>>, Boolean>comparing(e -> e.getValue().b())
                                .thenComparing(e -> e.getValue().a(), Comparator.reverseOrder()))
                        .toCustomMap(LinkedHashMap::new);
        child.setFullACL(EntryStream.of(sortedUsers)
                .map(e -> new ACLEntry(e.getKey().getId(), e.getKey().getDisplayName(), e.getKey().getUsername(),  e.getValue().a(), e.getValue().b())).sortedBy(e -> - e.getLevel().ordinal()).toArray(ACLEntry[]::new)
        );
        child.setShortACL(StreamEx.of(child.getFullACL())
                .filter(e -> e.getLevel() != IMPLICIT_VIEW)
                .limit(3)
                .toArray(ACLEntry[]::new));
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
}
