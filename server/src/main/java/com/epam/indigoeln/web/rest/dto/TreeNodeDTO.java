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
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.util.SequenceIdUtil;
import com.mongodb.DBObject;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for representing a Project, Notebook or an Experiment like a Tree Node with its properties.
 */
public class TreeNodeDTO {

    private String id;
    private String fullId;
    private String name;
    private Long version;
    public static final Comparator<TreeNodeDTO> NAME_COMPARATOR =
            (o1, o2) -> o1.getName().compareTo(o2.getName());
    private HashSet<UserPermission> accessList;

    TreeNodeDTO() {
        super();
    }

    TreeNodeDTO(BasicModelObject obj) {
        this.id = SequenceIdUtil.extractShortId(obj);
        this.fullId = obj.getId();
        this.name = obj.getName();
        this.version = obj.getVersion();
        this.accessList = new HashSet<>(obj.getAccessList());
    }

    public TreeNodeDTO(DBObject obj) {
        this.id = SequenceIdUtil.extractShortId(String.valueOf(obj.get("_id")));
        this.fullId = String.valueOf(obj.get("_id"));
        this.name = String.valueOf(obj.get("name"));
        this.version = (long)obj.get("version");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullId() {
        return fullId;
    }

    public void setFullId(String fullId) {
        this.fullId = fullId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public void setAccessList(Set<UserPermission> accessList) {
        this.accessList = accessList == null ? null : new HashSet<>(accessList);
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
