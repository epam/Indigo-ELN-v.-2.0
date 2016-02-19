package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.BasicModelObject;
import com.epam.indigoeln.core.util.SequenceIdUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A DTO for representing a Project, Notebook or an Experiment like a Tree Node with its properties like as
 * "hasChildren" flag
 */
public class TreeNodeDTO implements Serializable, Comparable<TreeNodeDTO> {

    private static final long serialVersionUID = -5561498961856213253L;

    public static List<TreeNodeDTO> convertAll(Collection<? extends BasicModelObject> nodes) {
        return nodes != null ? nodes.stream().map(TreeNodeDTO::new).sorted().collect(Collectors.toList()) : null;
    }


    private String id;
    private String fullId;
    private String name;
    private List<TreeNodeDTO> children;

    public TreeNodeDTO() {
    }

    public TreeNodeDTO(BasicModelObject obj) {
        this.id = SequenceIdUtil.extractShortId(obj);
        this.fullId = obj.getId();
        this.name = obj.getName();
    }

    public TreeNodeDTO(BasicModelObject obj, Collection<? extends BasicModelObject> children) {
        this(obj);
        this.children = convertAll(children);
    }

    public String getId() {
        return id;
    }

    public String getFullId() {
        return fullId;
    }

    public List<TreeNodeDTO> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setChildren(List<TreeNodeDTO> children) {
        this.children = children;
    }

    public void setChildren(Collection<? extends BasicModelObject> children) {
        this.children = convertAll(children);
    }

    @Override
    public int compareTo(TreeNodeDTO o) {
        return this.name.compareTo(o.getName());
    }
}