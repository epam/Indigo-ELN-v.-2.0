package com.epam.indigoeln.core.model;

import com.epam.indigoeln.core.security.Authority;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@ToString
@EqualsAndHashCode
@Document(collection = Role.COLLECTION_NAME)
public class Role implements Serializable {
    public static final String COLLECTION_NAME = "role";
    private static final long serialVersionUID = 1883639262323115257L;

    @Id
    private String id;

    private String name;

    private boolean system;

    @JsonIgnore
    transient
    private HashSet<Authority> authorities = new HashSet<>();

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
        this.authorities = new HashSet<>(authorities);
    }
}
