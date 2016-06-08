package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.security.Authority;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

public class RoleDTO {

    private String id;

    @NotNull
    @Size(min = 1, max = 50)
    private String name;

    private boolean system;

    private Set<Authority> authorities;

    public RoleDTO() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String toString() {
        return "RoleDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", authorities=" + authorities +
                "}";
    }
}