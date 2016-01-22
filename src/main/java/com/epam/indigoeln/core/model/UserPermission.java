package com.epam.indigoeln.core.model;

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

    public static final String READ_ENTITY = "RE";
    public static final String UPDATE_ENTITY = "UE";
    public static final String READ_SUB_ENTITY = "RS";
    public static final String CREATE_SUB_ENTITY = "CS";

    public static final String VIEWER_PERMISSIONS = READ_ENTITY;
    public static final String CHILD_VIEWER_PERMISSIONS = VIEWER_PERMISSIONS + READ_SUB_ENTITY;
    public static final String USER_PERMISSIONS = CHILD_VIEWER_PERMISSIONS + CREATE_SUB_ENTITY;
    public static final String OWNER_PERMISSIONS = USER_PERMISSIONS + UPDATE_ENTITY;

    private String userId;

    private String permissions;

    public UserPermission() {

    }

    public UserPermission(String userId, String permissions) {
        this.userId = userId;
        this.permissions = permissions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
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
        return permission != null && permission.length() == 2
                && this.permissions.contains(permission);
    }
}