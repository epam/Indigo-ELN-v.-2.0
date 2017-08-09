package com.epam.indigoeln.core.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@ToString
@EqualsAndHashCode
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

    private List<SequenceId> children;


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
}


