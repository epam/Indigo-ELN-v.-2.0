package com.epam.indigoeln.web.rest.util;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.security.Authority;
import com.epam.indigoeln.core.service.exception.EntityNotFoundException;
import com.epam.indigoeln.core.service.exception.PermissionIncorrectException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public final class PermissionUtil {

    private PermissionUtil() {
    }

    public static boolean isContentEditor(User user) {
        return user != null &&
                user.getAuthorities().contains(Authority.CONTENT_EDITOR);
    }

    /**
     * Search for {@link UserPermission} for user with userId in accessList.
     *
     * @param accessList access list of some resource
     * @param userId     identify of user whose permission on the entity should be founded
     * @return {@code null} if there is no permissions for user with userId in accessList
     * or {@link UserPermission} from accessList for this user
     */
    public static UserPermission findPermissionsByUserId(Set<UserPermission> accessList,
                                                         String userId
    ) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUser().getId().equals(userId)) {
                return userPermission;
            }
        }
        return null;
    }

    public static boolean hasPermissions(String userId,
                                         Set<UserPermission> accessList,
                                         String permission) {
        // Check of UserPermission
        UserPermission userPermission = findPermissionsByUserId(accessList, userId);
        return userPermission != null && userPermission.hasPermission(permission);
    }

    public static boolean hasEditorAuthorityOrPermissions(User user,
                                                          Set<UserPermission> accessList,
                                                          String permission) {
        return isContentEditor(user) || hasPermissions(user.getId(), accessList, permission);
    }

    public static boolean hasPermissions(String userId, Set<UserPermission> parentAccessList,
                                         String parentPermission,
                                         Set<UserPermission> childAccessList,
                                         String childPermission) {
        return hasPermissions(userId, parentAccessList, parentPermission)
                && hasPermissions(userId, childAccessList, childPermission);
    }

    /**
     * Adding of OWNER's permissions to Entity Access List for specified User, if it is absent.
     *
     * @param accessList              Access list
     * @param user                    User
     * @param permissionCreationLevel level of permission adding
     */
    public static void addOwnerToAccessList(Set<UserPermission> accessList,
                                            User user,
                                            PermissionCreationLevel permissionCreationLevel
    ) {
        UserPermission userPermission = findPermissionsByUserId(accessList, user.getId());
        if (userPermission != null) {
            userPermission.setPermissions(UserPermission.OWNER_PERMISSIONS);
            userPermission.setPermissionCreationLevel(permissionCreationLevel);
        } else {
            userPermission = new UserPermission(user, UserPermission.OWNER_PERMISSIONS);
            userPermission.setPermissionCreationLevel(permissionCreationLevel);
            accessList.add(userPermission);
        }
    }

    /**
     * Add permission from upper level permissions to current permissions.
     *
     * @param accessList                current entity permissions
     * @param upperLevelUserPermissions permissions of upper level entity
     * @param upperPermissionsLevel     upper entity permission level
     */
    public static void addUsersFromUpperLevel(Set<UserPermission> accessList,
                                              Set<UserPermission> upperLevelUserPermissions,
                                              PermissionCreationLevel upperPermissionsLevel
    ) {
        upperLevelUserPermissions.forEach(up -> {
            if (canBeAddedFromUpperLevel(upperPermissionsLevel, up)) {
                accessList.add(up);
            }
        });
    }

    /**
     * Check if permission can be added from some level.
     *
     * @param upperPermissionsLevel Upper permission level
     * @param userPermission        User permission
     * @return {@code true} if {@code upperPermissionsLevel} is lower or equal to {@code userPermission} creation level.
     */
    private static boolean canBeAddedFromUpperLevel(PermissionCreationLevel upperPermissionsLevel,
                                                    UserPermission userPermission
    ) {
        return userPermission != null
                && canBeChangedFromThisLevel(upperPermissionsLevel, userPermission.getPermissionCreationLevel());
    }

    public static void checkCorrectnessOfAccessList(UserRepository userRepository,
                                                    Set<UserPermission> accessList) {
        for (UserPermission userPermission : accessList) {
            if (userPermission.getUser() == null) {
                throw PermissionIncorrectException.createWithoutUserId();
            }

            User userFromDB = userRepository.findOne(userPermission.getUser().getId());
            if (userFromDB == null) {
                throw EntityNotFoundException.createWithUserId(userPermission.getUser().getId());
            }
            userPermission.setUser(userFromDB);

            // check correctness of permissions
            for (String permission : userPermission.getPermissions()) {
                if (!UserPermission.ALL_PERMISSIONS.contains(permission)) {
                    throw PermissionIncorrectException.createWithUserIdAndPermission(
                            userPermission.getUser().getId(), permission);
                }
            }

            if (userPermission.getUser().getAuthorities().contains(Authority.CONTENT_EDITOR)
                    && !UserPermission.OWNER.equals(userPermission.getPermissionView())) {
                throw PermissionIncorrectException
                        .createWithUserIdAndPermission(userPermission.getUser().getId(),
                                userPermission.getPermissionView());
            }
        }
    }

    /**
     * Change experiments access list according to {@code newUserPermissions}.
     * <p>
     * This method calls changing of notebook and project permissions as well
     * and returns the pair of two boolean flags these mean (notebook was changed, project was changed).
     *
     * @param project            Project that contains {@code notebook}
     * @param notebook           Notebook that contains changing {@code experiment}
     * @param experiment         Experiment to change permissions
     * @param newUserPermissions permissions that should be applied
     * @return pair of two boolean flags these mean (notebook was changed, project was changed).
     */
    public static Pair<Boolean, Boolean> changeExperimentPermissions(Project project,
                                                                     Notebook notebook,
                                                                     Experiment experiment,
                                                                     Set<UserPermission> newUserPermissions
    ) {
        boolean notebookHadChanged = false;
        boolean projectHadChanged = false;

        EntityWrapper wrappedExperiment = new EntityWrapper.ExperimentWrapper(project, notebook, experiment);

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> experiment.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.EXPERIMENT))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            wrappedExperiment.addPermissionsDown(createdPermissions);

            Set<UserPermission> addedToNotebook = wrappedExperiment.addOrUpdatePermissionsUpForOneLevel(createdPermissions);
            notebookHadChanged = !addedToNotebook.isEmpty();

            if (!addedToNotebook.isEmpty()) {
                projectHadChanged = !wrappedExperiment.getParent().addOrUpdatePermissionsUpForOneLevel(addedToNotebook)
                        .isEmpty();
            }
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> experiment.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.EXPERIMENT))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            wrappedExperiment.updatePermissionsDown(updatedPermissions);

            Set<UserPermission> permissionsUpdatedInNotebook =
                    wrappedExperiment.addOrUpdatePermissionsUpForOneLevel(updatedPermissions);

            notebookHadChanged |= !permissionsUpdatedInNotebook.isEmpty();

            if (!permissionsUpdatedInNotebook.isEmpty()) {
                projectHadChanged |=
                        !wrappedExperiment.getParent().addOrUpdatePermissionsUpForOneLevel(permissionsUpdatedInNotebook).isEmpty();
            }
        }

        Set<UserPermission> removedPermissions = experiment.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {

            wrappedExperiment.removePermissionsDown(removedPermissions);

            Set<UserPermission> removedFromNotebook = wrappedExperiment.removePermissionsUpForOneLevel(removedPermissions);

            notebookHadChanged |= !removedFromNotebook.isEmpty();

            if (!removedFromNotebook.isEmpty()) {
                projectHadChanged |= !wrappedExperiment.getParent().removePermissionsUpForOneLevel(removedFromNotebook).isEmpty();
            }
        }

        return Pair.of(notebookHadChanged, projectHadChanged);
    }

    /**
     * Change notebooks access list according to {@code newUserPermissions}.
     * <p>
     * This method calls changing of project permissions as well and returns boolean flags
     * that mean was project changed or not.
     *
     * @param project            Project that contains {@code notebook}
     * @param notebook           Notebook to change permissions
     * @param newUserPermissions permissions that should be applied
     * @return {@code true} if project's access list was changed.
     */
    public static boolean changeNotebookPermissions(Project project,
                                                    Notebook notebook,
                                                    Set<UserPermission> newUserPermissions
    ) {
        boolean projectHadChanged = false;

        EntityWrapper notebookWrapper = new EntityWrapper.NotebookWrapper(project, notebook);

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> notebook.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.NOTEBOOK))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {

            projectHadChanged = !notebookWrapper.addOrUpdatePermissionsUpForOneLevel(createdPermissions).isEmpty();

            notebookWrapper.addPermissionsDown(createdPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> notebook.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.NOTEBOOK))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            notebookWrapper.updatePermissionsDown(updatedPermissions);

            projectHadChanged |= !notebookWrapper.addOrUpdatePermissionsUpForOneLevel(updatedPermissions).isEmpty();
        }

        Set<UserPermission> removedPermissions = notebook.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {
            notebookWrapper.removePermissionsDown(removedPermissions);

            Set<UserPermission> removed = notebookWrapper.removePermissionsUpForOneLevel(removedPermissions);

            projectHadChanged |= !removed.isEmpty();
        }
        return projectHadChanged;
    }

    /**
     * Change projects access list according to {@code newUserPermissions}.
     *
     * @param project            project to change permissions
     * @param newUserPermissions permissions that should be applied
     */
    public static void changeProjectPermissions(Project project, Set<UserPermission> newUserPermissions) {

        EntityWrapper.ProjectWrapper projectWrapper = new EntityWrapper.ProjectWrapper(project);

        Set<UserPermission> createdPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream()
                        .noneMatch(oldPermission ->
                                equalsByUserId(newPermission, oldPermission)))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        if (!createdPermissions.isEmpty()) {
            projectWrapper.addPermissionsDown(createdPermissions);
        }

        Set<UserPermission> updatedPermissions = newUserPermissions.stream()
                .filter(newPermission -> project.getAccessList().stream().anyMatch(oldPermission ->
                        equalsByUserId(newPermission, oldPermission)
                                && !oldPermission.getPermissions().equals(newPermission.getPermissions())))
                .map(userPermission -> userPermission.setPermissionCreationLevel(PermissionCreationLevel.PROJECT))
                .collect(toSet());

        if (!updatedPermissions.isEmpty()) {
            projectWrapper.updatePermissionsDown(updatedPermissions);
        }

        Set<UserPermission> removedPermissions = project.getAccessList().stream().filter(oldPermission ->
                newUserPermissions.stream().noneMatch(newPermission -> equalsByUserId(oldPermission, newPermission)))
                .collect(toSet());

        if (!removedPermissions.isEmpty()) {
            projectWrapper.removePermissionsDown(removedPermissions);
        }
    }

    /**
     * Check if these permissions are for the same user.
     *
     * @param oldPermission Old permission
     * @param newPermission New permission
     * @return {@code true} if permissions are for the same user
     */
    public static boolean equalsByUserId(UserPermission oldPermission, UserPermission newPermission) {
        return Objects.equals(newPermission.getUser().getId(), oldPermission.getUser().getId());
    }

    /**
     * Check if permission from current level can change changing permission.
     *
     * @param changingPermissionLevel Changing permission level
     * @param currentLevel            Current level
     * @return @code true} if entities from {@code changingPermissionLevel} can be changed
     * by permissions of {@code currentLevel}
     */
    public static boolean canBeChangedFromThisLevel(PermissionCreationLevel changingPermissionLevel,
                                                    PermissionCreationLevel currentLevel
    ) {
        return changingPermissionLevel != null
                && currentLevel != null
                && Comparator.comparing(PermissionCreationLevel::ordinal)
                .compare(changingPermissionLevel, currentLevel) >= 0;
    }


}