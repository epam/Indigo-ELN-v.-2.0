package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Set;

/**
 * UserPermission contains types of permissions on entity for User.<br/>
 * User can be <b>VIEWER</b>, <b>CHILD_VIEWER</b>, <b>USER</b> or <b>OWNER</b> in relation to Entity.<br/>
 * By roles:
 * <ul>
 * <li><b>VIEWER</b> can only <b>read Entity</b></li>
 * <li><b>USER</b> can <b>create Sub-Entity</b> and all above</li>
 * <li><b>OWNER</b> can <b>update Entity</b> and all above</li>
 * </ul>
 */
@ToString
@EqualsAndHashCode(exclude = "permissionCreationLevel")
public class UserPermission {

    public static final String READ_ENTITY = "READ_ENTITY";
    public static final String UPDATE_ENTITY = "UPDATE_ENTITY";
    public static final String CREATE_SUB_ENTITY = "CREATE_SUB_ENTITY";

    public static final String VIEWER = "VIEWER";
    public static final String USER = "USER";
    public static final String OWNER = "OWNER";

    public static final Set<String> VIEWER_PERMISSIONS = ImmutableSet.of(READ_ENTITY);
    public static final Set<String> USER_PERMISSIONS =
            ImmutableSet.of(READ_ENTITY, CREATE_SUB_ENTITY);
    public static final Set<String> OWNER_PERMISSIONS =
            ImmutableSet.of(READ_ENTITY, CREATE_SUB_ENTITY, UPDATE_ENTITY);
    public static final Set<String> ALL_PERMISSIONS = OWNER_PERMISSIONS;

    @DBRef
    private User user;

    private Set<String> permissions;

    private Boolean removable = true;

    @JsonIgnore
    private PermissionCreationLevel permissionCreationLevel;

    public UserPermission() {
        super();
    }

    public UserPermission(User user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    public UserPermission(User user,
                          Set<String> permissions,
                          PermissionCreationLevel permissionCreationLevel,
                          Boolean removable
    ) {
        this.user = user;
        this.permissions = permissions;
        this.removable = removable;
        this.permissionCreationLevel = permissionCreationLevel;
    }

    public UserPermission(User user, Set<String> permissions, PermissionCreationLevel permissionCreationLevel) {
        this.user = user;
        this.permissions = permissions;
        this.permissionCreationLevel = permissionCreationLevel;
    }

    public Boolean isRemovable() {
        return removable;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public UserPermission setPermissions(Set<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public boolean canReadEntity() {
        return permissions.contains(READ_ENTITY);
    }

    public boolean canUpdateEntity() {
        return permissions.contains(UPDATE_ENTITY);
    }

    public boolean canCreateSubEntity() {
        return permissions.contains(CREATE_SUB_ENTITY);
    }

    public boolean hasPermission(String permission) {
        return permission != null && this.permissions.contains(permission);
    }

    public String getPermissionView() {
        if (OWNER_PERMISSIONS.size() == permissions.size() && permissions.containsAll(OWNER_PERMISSIONS)) {
            return OWNER;
        }
        if (USER_PERMISSIONS.size() == permissions.size() && permissions.containsAll(USER_PERMISSIONS)) {
            return USER;
        }
        if (VIEWER_PERMISSIONS.size() == permissions.size() && permissions.containsAll(VIEWER_PERMISSIONS)) {
            return VIEWER;
        }
        return null;
    }

    public PermissionCreationLevel getPermissionCreationLevel() {
        return permissionCreationLevel;
    }

    public UserPermission setPermissionCreationLevel(PermissionCreationLevel permissionCreationLevel) {
        this.permissionCreationLevel = permissionCreationLevel;
        return this;
    }
}
