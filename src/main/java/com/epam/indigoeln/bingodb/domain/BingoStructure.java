package com.epam.indigoeln.bingodb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Entity for holding structure with its ID
 */
public class BingoStructure implements Serializable {

    /**
     * Structure ID
     */
    private String id;

    /**
     * Structure
     */
    private String structure;

    /**
     * Create a new BingoStructure instance
     */
    public BingoStructure() {
        // Empty constructor for JSON deserialize
    }

    /**
     * Create a new BingoStructure instance
     * @param id structure id
     * @param structure structure
     */
    public BingoStructure(String id, String structure) {
        this.id = id;
        this.structure = structure;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BingoStructure that = (BingoStructure) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(structure, that.structure)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(structure)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("structure", structure)
                .toString();
    }
}
