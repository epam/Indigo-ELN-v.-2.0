package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.model.UserPermission;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for representing a Project, Notebook or an Experiment like a Tree Node with its properties
 */
public class TreeNodeDTO {

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

    public static final Comparator<TreeNodeDTO> NAME_COMPARATOR =
            (o1, o2) -> o1.getName().compareTo(o2.getName());

}