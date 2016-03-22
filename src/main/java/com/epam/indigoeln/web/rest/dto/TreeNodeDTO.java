package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO for representing a Project, Notebook or an Experiment like a Tree Node with its properties
 */
public class TreeNodeDTO implements Serializable, Comparable<TreeNodeDTO> {

    private static final long serialVersionUID = -5561498961856213253L;

    private String id;
    private String fullId;
    private String name;
    private String status;
    private HashSet<UserPermission> accessList;

    public TreeNodeDTO() {
        super();
    }

    public TreeNodeDTO(BasicModelObject obj) {
        this.id = SequenceIdUtil.extractShortId(obj);
        this.fullId = obj.getId();
        this.name = obj.getName();
        this.accessList = new HashSet<>(obj.getAccessList());
    }

    public String getId() {
        return id;
    }

    public String getFullId() {
        return fullId;
    }

    public String getName() {
        return name;
    }

    public Set<UserPermission> getAccessList() {
        return accessList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(TreeNodeDTO o) {
        return this.name.compareTo(o.getName());
    }
}