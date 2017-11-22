package com.epam.indigoeln.bingodb.web.rest.dto;

import com.epam.indigoeln.bingodb.domain.BingoStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Simple DTO for REST response.
 */
public class ResponseDTO implements Serializable {

    /**
     * Structures to response.
     */
    private List<BingoStructure> structures;

    /**
     * Create a new ResponseDTO instance.
     */
    public ResponseDTO() {
        // Empty constructor for JSON deserialize
    }

    /**
     * Create a new ResponseDTO instance.
     *
     * @param structures structures to response
     */
    public ResponseDTO(List<BingoStructure> structures) {
        this.structures = structures;
    }

    /**
     * Get an existing list of structures.
     *
     * @return existing list of structures
     */
    public List<BingoStructure> getStructures() {
        return structures;
    }

    /**
     * Set a new list of structures.
     *
     * @param structures new list of structures to set
     */
    public void setStructures(List<BingoStructure> structures) {
        this.structures = structures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResponseDTO that = (ResponseDTO) o;

        return new EqualsBuilder()
                .append(structures, that.structures)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(structures)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("structures", structures)
                .toString();
    }
}
