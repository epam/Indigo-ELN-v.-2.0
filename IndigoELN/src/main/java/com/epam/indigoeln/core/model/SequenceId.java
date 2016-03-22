package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Document(collection = SequenceId.COLLECTION_NAME)
public class SequenceId implements Serializable {

    public static final String COLLECTION_NAME = "sequence_id";
    private static final long serialVersionUID = 2661973943428932165L;

    @Id
    private String id;

    @NotNull
    private Long sequence;

    @Version
    private Long version;

    List<SequenceId> children;


    public SequenceId() {
        super();
    }

    public SequenceId(Long sequence) {
        this.sequence = sequence;
    }

    public SequenceId(String id, Long sequence) {
        this.id = id;
        this.sequence = sequence;
    }

    public String getId() {
        return id;
    }

    public Long getSequence() {
        return sequence;
    }

    public List<SequenceId> getChildren() {
        return children;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public void setChildren(List<SequenceId> children) {
        this.children = children;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SequenceId)) {
            return false;
        }
        SequenceId that = (SequenceId) o;
        return  Objects.equal(id, that.id) &&
                Objects.equal(sequence, that.sequence) &&
                Objects.equal(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, sequence, children);
    }

    @Override
    public String toString() {
        return "SequenceId{" +
                "id='" + id + '\'' +
                ", sequence='" + sequence + '\'' +
                ", children=" + children +
                '}';
    }
}


