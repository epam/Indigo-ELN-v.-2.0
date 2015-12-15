package com.epam.indigoeln.auth;

import java.util.Collection;

public class UserInfo {

    private String userId;
    private String username;
    private Collection<String> roles;
    private Collection<String> permissions;

    public UserInfo() {
    }

    public UserInfo(final String userId, final String username, final Collection<String> roles,
                    final Collection<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public Collection<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<String> permissions) {
        this.permissions = permissions;
    }
}
