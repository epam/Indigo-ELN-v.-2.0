package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserPermission;

import java.util.Set;

public class UserPermissionDTO {

    private User user;

    private Set<String> permissions;

    private String permissionView;

    private Boolean removable;

    public UserPermissionDTO() {
        super();
    }

    UserPermissionDTO(UserPermission userPermission) {
        this.user = userPermission.getUser();
        this.permissions = userPermission.getPermissions();
        this.permissionView = userPermission.getPermissionView();
        this.removable = userPermission.isRemovable();
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

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getPermissionView() {
        return permissionView;
    }

    public void setPermissionView(String permissionView) {
        this.permissionView = permissionView;
    }

    public Boolean getRemovable() {
        return removable;
    }

    public void setRemovable(Boolean removable) {
        this.removable = removable;
    }
}
