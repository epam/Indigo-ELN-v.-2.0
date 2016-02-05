package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;

/**
 * A DTO for representing a Project, Notebook or an Experiment like a Tree Node with its properties like as
 * "hasChildren" flag
 */
public class TreeNodeDTO implements Serializable {
    boolean hasChildren;
    Object node;

    public TreeNodeDTO() {
    }

    public TreeNodeDTO(Object node) {
        this.node = node;
    }

    public TreeNodeDTO(Object node, boolean hasChildren) {
        this.node = node;
        this.hasChildren = hasChildren;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Object getNode() {
        return node;
    }

    public void setNode(Object node) {
        this.node = node;
    }
}