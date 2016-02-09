package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Document(collection = "sequence_id")
public class SequenceId implements Serializable {

    private static final long serialVersionUID = 2661973943428932165L;

    @Id
    private String id;
    private Long seq;

    public String getId() {
        return id;
    }

    public Long getSeq() {
        return seq;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SequenceId that = (SequenceId) o;
        return  Objects.equal(id, that.id) &&
                Objects.equal(seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, seq);
    }

    @Override
    public String toString() {
        return "SequenceId{" +
                "id='" + id + '\'' +
                ", seq=" + seq +
                '}';
    }
}


