package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.UserPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public abstract class BasicDTO implements Serializable {

    private static final long serialVersionUID = -2304109712431441621L;

    @JsonProperty("id")
    private Long sequenceId;
    private String name;
    private Set<UserPermission> accessList = new HashSet<>();

    //Read-Only auto-generated audit parameters
    private UserDTO   author;
    private UserDTO   lastModifiedBy;
    private LocalDate lastEditDate;
    private LocalDate creationDate;

    public BasicDTO() {
    }

    public BasicDTO(Long sequenceId,
                    String name,
                    Set<UserPermission> accessList,
                    UserDTO author,
                    UserDTO lastModifiedBy,
                    LocalDate creationDate,
                    LocalDate lastEditDate) {
        this.sequenceId = sequenceId;
        this.name = name;
        this.accessList = accessList;
        this.author = author;
        this.lastModifiedBy = lastModifiedBy;
        this.lastEditDate = lastEditDate;
        this.creationDate = creationDate;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public String getName() {
        return name;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public UserDTO getLastModifiedBy() {
        return lastModifiedBy;
    }

    public LocalDate getLastEditDate() {
        return lastEditDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList;
    }

    @Override
    public String toString() {
        return "BasicDTO{" +
                "sequenceId=" + sequenceId +
                ", name='" + name + '\'' +
                '}';
    }
}
