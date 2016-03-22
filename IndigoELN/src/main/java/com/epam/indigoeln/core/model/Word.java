package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;

public class Word extends BasicModelObject{

    private static final long serialVersionUID = 3317184304499940837L;

    private String description;
    private boolean enable;
    private Integer rank;

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

    @Override
    public String toString() {
        return "Word{" +
                "description='" + description + '\'' +
                "enable='" + enable + '\'' +
                "rank='" + rank + '\'' +
                "} " + super.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), description, enable, rank);
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
        return  Objects.equal(description, w.description) &&
                Objects.equal(rank, w.rank) &&
                Objects.equal(enable, w.enable);
    }
}
