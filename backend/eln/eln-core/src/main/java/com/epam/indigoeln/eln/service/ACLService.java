package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.AccessDeniedException;
import com.epam.indigoeln.common.exception.InvalidRequestException;
import com.epam.indigoeln.eln.entity.*;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.model.AccessOperation;
import com.epam.indigoeln.eln.model.ApplicationRole;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.repository.ExperimentRepository;
import com.epam.indigoeln.eln.repository.NotebookRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;

import java.util.HashMap;
import java.util.Map;
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

    public void ensureTopLevelAccess(AccessOperation operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        throw new AccessDeniedException(operation, userService.getCurrentUser().getUsername(), userService.getCurrentUser().getRoles());
    }

    public void ensureAccess(ProjectEntity project, AccessOperation operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        if (!operation.isAllowedBy(project.getCurrentAccess())) {
            throw new AccessDeniedException(EntityType.PROJECT, project.getId(), operation, project.getCurrentAccess(), userService.getCurrentUser().getUsername(), userService.getCurrentUser().getRoles());
        }
    }

    public void ensureAccess(NotebookEntity notebook, AccessOperation operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        if (!operation.isAllowedBy(notebook.getCurrentAccess())) {
            throw new AccessDeniedException(EntityType.NOTEBOOK, notebook.getId(), operation, notebook.getCurrentAccess(), userService.getCurrentUser().getUsername(), userService.getCurrentUser().getRoles());
        }
    }

    public void ensureAccess(ExperimentEntity experiment, AccessOperation operation) {
        if (isUserRolesAllow(operation)) {
            return;
        }
        if (!operation.isAllowedBy(experiment.getCurrentAccess())) {
            throw new AccessDeniedException(EntityType.EXPERIMENT, experiment.getId(), operation, experiment.getCurrentAccess(), userService.getCurrentUser().getUsername(), userService.getCurrentUser().getRoles());
        }
    }

    private boolean isUserRolesAllow(AccessOperation operation) {
        for (ApplicationRole role : userService.getCurrentUser().getRoles()) {
            if (role.allows(operation)) {
                return true;
            }
        }
        return false;
    }

    public void initProjectACL(ProjectEntity project) {
        project.setAclEntities(new HashMap<>(1));
        applyAccess(project, userService.getCurrentUser(), AUTHOR, false);
    }

    public void initNotebookACL(NotebookEntity notebook) {
        ProjectEntity project = notebook.getProject();
        Map<UserEntity, NotebookACLEntity> acl = EntryStream.of(project.getAclEntities())
                .mapValues(pa -> new NotebookACLEntity(notebook, pa.getUser(), pa.getLevel() != AUTHOR ? pa.getLevel() : ADMIN, true))
                .toMap();
        notebook.setAclEntities(acl);
        applyAccess(notebook, userService.getCurrentUser(), AUTHOR, false);
        if (!project.getAclEntities().containsKey(userService.getCurrentUser())) {
            project.getAclEntities().put(userService.getCurrentUser(), new ProjectACLEntity(project, userService.getCurrentUser(), IMPLICIT_VIEW));
        }
    }

    public void initExperimentACL(ExperimentEntity experiment) {
        NotebookEntity notebook = experiment.getNotebook();
        Map<UserEntity, ExperimentACLEntity> acl = EntryStream.of(notebook.getAclEntities())
                .mapValues(pa -> new ExperimentACLEntity(experiment, pa.getUser(), pa.getLevel() != AUTHOR ? pa.getLevel() : ADMIN, true))
                .toMap();
        experiment.setAclEntities(acl);
        applyAccess(experiment, userService.getCurrentUser(), AUTHOR, false);
        if (!notebook.getAclEntities().containsKey(userService.getCurrentUser())) {
            notebook.getAclEntities().put(userService.getCurrentUser(), new NotebookACLEntity(notebook, userService.getCurrentUser(), IMPLICIT_VIEW, false));
            ProjectEntity project = notebook.getProject();
            if (!project.getAclEntities().containsKey(userService.getCurrentUser())) {
                project.getAclEntities().put(userService.getCurrentUser(), new ProjectACLEntity(project, userService.getCurrentUser(), IMPLICIT_VIEW));
            }
        }
    }

    public void updateProjectACL(ProjectEntity project, UserEntity user, AccessLevel level) {
        // preload all ACL lists
        notebookRepository.findByProjectWithACLEntities(project);
        experimentRepository.findByProjectWithACLEntities(project);

        if (applyAccess(project, user, level, false)) {
            for (NotebookEntity notebook : project.getNotebooks()) {
                if (applyNestedAccess(notebook, user, level)) {
                    for (ExperimentEntity experiment : notebook.getExperiments()) {
                        applyNestedAccess(experiment, user, level);
                    }
                }
            }
        }
    }

    public void updateNotebookACL(ProjectEntity project, NotebookEntity notebook, UserEntity user, AccessLevel level) {
        // preload all ACL lists
        experimentRepository.findByNotebookWithACLEntities(notebook);

        if (applyAccess(notebook, user, level, false)) {
            for (ExperimentEntity experiment : notebook.getExperiments()) {
                applyNestedAccess(experiment, user, level);
            }
            applyImplicitAccess(project, user, level, () -> notebookRepository.hasAccessibleNotebooks(project));
        }
    }

    public void updateExperimentACL(ProjectEntity project, NotebookEntity notebook, ExperimentEntity experiment, UserEntity user, AccessLevel level) {
        if (applyAccess(experiment, user, level, false)) {
            if (!notebook.getAclEntities().containsKey(user)) {
                notebook.getAclEntities().put(user, new NotebookACLEntity(notebook, user, IMPLICIT_VIEW, false));
                if (!project.getAclEntities().containsKey(user)) {
                    project.getAclEntities().put(user, new ProjectACLEntity(project, user, IMPLICIT_VIEW));
                }
            }
            if (applyImplicitAccess(notebook, user, level, () -> experimentRepository.hasAccessibleExperiments(notebook))) {
                applyImplicitAccess(project, user, level, () -> notebookRepository.hasAccessibleNotebooks(project));
            }
        }
    }

    private boolean applyImplicitAccess(WithACL<?> container, UserEntity user, AccessLevel childLevel, Supplier<Boolean> isAccessible) {
        BaseACLEntity entry = container.getAclEntities().get(user);
        if (childLevel != AccessLevel.NONE && entry == null) {
            return applyAccess(container, user, IMPLICIT_VIEW, false);
        } else if (childLevel == AccessLevel.NONE && entry != null && entry.getLevel() == IMPLICIT_VIEW && !isAccessible.get()) {
            return applyAccess(container, user, AccessLevel.NONE, false);
        }
        return false;
    }

    private boolean applyNestedAccess(WithACL<?> child, UserEntity user, AccessLevel level) {
        BaseACLEntity entry = child.getAclEntities().get(user);
        if (entry == null || entry.getInherited() || entry.getLevel() == IMPLICIT_VIEW) {
            return applyAccess(child, user, level != AUTHOR ? level : ADMIN, true);
        }
        return false;
    }

    private boolean applyAccess(WithACL<?> container, UserEntity user, AccessLevel level, boolean inherited) {
        if (user.equals(container.getCreatedBy()) && level != AUTHOR) {
            throw new InvalidRequestException("AUTHOR permission cannot be removed from " + user.getUsername());
        }
        if (level == AUTHOR && !user.equals(container.getCreatedBy())) {
            throw new InvalidRequestException("Cannot assign AUTHOR permission to anyone else");
        }
        if (level == AccessLevel.NONE) {
            boolean removed = container.getAclEntities().remove(user) != null;
            if (removed) {
                log.debug("applyAccess: removed {} from {}", user, container);
            }
            return removed;
        }
        BaseACLEntity entry = container.getAclEntities().get(user);
        if (entry == null) {
            container.insertACL(user, level, inherited);
            log.debug("applyAccess: inserted {} inherited={} for {} to {}", level, inherited, user, container);
            return true;
        }
        if (entry.getLevel() != level || entry.getInherited() != inherited) {
            log.debug("applyAccess: updated {}->{}, inherited {}->{} for {} in {}", entry.getLevel(), level, entry.getInherited(), inherited, user, container);
            entry.setLevel(level);
            entry.setInherited(inherited);
            return true;
        }
        return false;
    }
}
