package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Document(collection = SequenceId.COLLECTION_NAME)
public class SequenceId implements Serializable {

    public static final String COLLECTION_NAME = "sequence_id";
    private static final long serialVersionUID = 2661973943428932165L;

    @Id
    private Long id;

    @Version
    private Long version;

    List<SequenceId> children;


    public SequenceId() {
    }

    public SequenceId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<SequenceId> getChildren() {
        return children;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChildren(List<SequenceId> children) {
        this.children = children;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceId)) return false;
        SequenceId that = (SequenceId) o;
        return  Objects.equal(id, that.id) &&
                Objects.equal(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, children);
    }

    @Override
    public String toString() {
        return "SequenceId{" +
                "id='" + id + '\'' +
                ", children=" + children +
                '}';
    }
}


