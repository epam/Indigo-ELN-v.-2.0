/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import jakarta.validation.constraints.NotEmpty;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class BasicDTO {

    private String id;
    private String parentId;
    private String fullId;

    @NotEmpty
    private String name;

    private Set<UserPermissionDTO> accessList = new HashSet<>();

    //Read-Only auto-generated audit parameters
    private UserDTO author;
    private UserDTO lastModifiedBy;

    private ZonedDateTime lastEditDate;

    private ZonedDateTime creationDate;

    private Long version;

    BasicDTO() {
    }

    BasicDTO(BasicModelObject modelObject) {
        this.fullId = modelObject.getId();
        this.id = SequenceIdUtil.extractShortId(modelObject);
        this.parentId = SequenceIdUtil.extractParentId(modelObject);
        this.name = modelObject.getName();
        this.author = modelObject.getAuthor() != null ? new UserDTO(modelObject.getAuthor()) : null;
        this.lastModifiedBy = modelObject.getLastModifiedBy() != null
                ? new UserDTO(modelObject.getLastModifiedBy()) : null;
        this.lastEditDate = modelObject.getLastEditDate();
        this.creationDate = modelObject.getCreationDate();
        if (modelObject.getAccessList() != null) {
            this.accessList.addAll(modelObject.getAccessList()
                    .stream()
                    .map(UserPermissionDTO::new)
                    .collect(Collectors.toList()));
        }
        this.version = modelObject.getVersion();
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

    public void setName(String name) {
        this.name = name;
    }

    public Set<UserPermissionDTO> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermissionDTO> accessList) {
        this.accessList = accessList;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "BasicDTO{"
                + "id=" + id
                + ", fullId='" + fullId + '\''
                + ", name='" + name + '\''
                + '}';
    }
}
