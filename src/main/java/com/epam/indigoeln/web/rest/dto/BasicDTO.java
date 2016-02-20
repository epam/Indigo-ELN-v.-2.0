package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class BasicDTO implements Serializable {

    private static final long serialVersionUID = -2304109712431441621L;

    private String id;
    private String parentId;
    private String fullId;

    @NotEmpty
    private String name;

    private Set<UserPermissionDTO> accessList = new HashSet<>();

    //Read-Only auto-generated audit parameters
    private UserDTO   author;
    private UserDTO   lastModifiedBy;

    private ZonedDateTime lastEditDate;

    private ZonedDateTime creationDate;

    public BasicDTO() {
    }

    protected BasicDTO(BasicModelObject modelObject) {
        this.fullId = modelObject.getId();
        this.id = SequenceIdUtil.extractShortId(modelObject);
        this.parentId = SequenceIdUtil.extractParentId(modelObject);
        this.name = modelObject.getName();
        this.author = modelObject.getAuthor() != null ? new UserDTO(modelObject.getAuthor()) : null;
        this.lastModifiedBy = modelObject.getLastModifiedBy() != null ? new UserDTO(modelObject.getLastModifiedBy()) : null;
        this.lastEditDate = modelObject.getLastEditDate();
        this.creationDate = modelObject.getCreationDate();
        if (modelObject.getAccessList() != null) {
            this.accessList.addAll(modelObject.getAccessList().stream().map(UserPermissionDTO::new).collect(Collectors.toList()));
        }
    }

    public String getId() {
        return id;
    }

    public String getFullId() {
        return fullId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public Set<UserPermissionDTO> getAccessList() {
        return accessList;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public UserDTO getLastModifiedBy() {
        return lastModifiedBy;
    }

    public ZonedDateTime getLastEditDate() {
        return lastEditDate;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccessList(Set<UserPermissionDTO> accessList) {
        this.accessList = accessList;
    }

    @Override
    public String toString() {
        return "BasicDTO{" +
                "id=" + id +
                ", fullId='" + fullId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
