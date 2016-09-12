package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

public class Word {

    private static final long serialVersionUID = 3317184304499940837L;

    private String id;
    private String name;
    private String description;
    private boolean enable;
    private Integer rank;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id='" + id + '\'' +
                "name='" + name + '\'' +
                "description='" + description + '\'' +
                "enable='" + enable + '\'' +
                "rank='" + rank + '\'' +
                "} " + super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), id, name, description, enable, rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Word)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Word w = (Word) o;
        return Objects.equal(id, w.id) &&
                Objects.equal(name, w.name) &&
                Objects.equal(description, w.description) &&
                Objects.equal(rank, w.rank) &&
                Objects.equal(enable, w.enable);
    }
}
