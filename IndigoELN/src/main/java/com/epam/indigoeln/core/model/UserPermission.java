package com.epam.indigoeln.core.model;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * UserPermission contains types of permissions on entity for User.<br/>
 * User can be <b>VIEWER</b>, <b>CHILD_VIEWER</b>, <b>USER</b> or <b>OWNER</b> in relation to Entity.<br/>
 * By roles:
 * <ul>
 *  <li><b>VIEWER</b> can only <b>read Entity</b></li>
 *  <li><b>CHILD_VIEWER</b> can <b>read Sub-Entity</b> and all above</li>
 *  <li><b>USER</b> can <b>create Sub-Entity</b> and all above</li>
 *  <li><b>OWNER</b> can <b>update Entity</b> and all above</li>
 * </ul>
 */
public class UserPermission {

    public static final String READ_ENTITY = "READ_ENTITY";
    public static final String UPDATE_ENTITY = "UPDATE_ENTITY";
    public static final String READ_SUB_ENTITY = "READ_SUB_ENTITY";
    public static final String CREATE_SUB_ENTITY = "CREATE_SUB_ENTITY";

    public static final List<String> VIEWER_PERMISSIONS = ImmutableList.of(READ_ENTITY);
    public static final List<String> CHILD_VIEWER_PERMISSIONS = ImmutableList.of(READ_ENTITY, READ_SUB_ENTITY);
    public static final List<String> USER_PERMISSIONS =
            ImmutableList.of(READ_ENTITY, READ_SUB_ENTITY, CREATE_SUB_ENTITY);
    public static final List<String> OWNER_PERMISSIONS =
            ImmutableList.of(READ_ENTITY, READ_SUB_ENTITY, CREATE_SUB_ENTITY, UPDATE_ENTITY);

    private String userId;

    private List<String> permissions;

    public UserPermission() {
    }

    public UserPermission(String userId, List<String> permissions) {
        this.userId = userId;
        this.permissions = permissions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean canReadEntity() {
        return permissions.contains(READ_ENTITY);
    }

    public boolean canUpdateEntity() {
        return permissions.contains(UPDATE_ENTITY);
    }

    public boolean canReadSubEntity() {
        return permissions.contains(READ_SUB_ENTITY);
    }

    public boolean canCreateSubEntity() {
        return permissions.contains(CREATE_SUB_ENTITY);
    }

    public boolean hasPermission(String permission) {
        return permission != null && this.permissions.contains(permission);
    }
}