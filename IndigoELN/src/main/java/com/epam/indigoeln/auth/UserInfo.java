package com.epam.indigoeln.auth;

import java.util.Collection;

public class UserInfo {

    private String username;
    private Collection<String> roles;

    public UserInfo() {
    }

    public UserInfo(final String username, final Collection<String> roles) {
        this.username = username;
        this.roles = roles;
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
}
