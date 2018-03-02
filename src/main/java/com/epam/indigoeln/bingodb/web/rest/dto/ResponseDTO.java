/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *
 *  This file is part of BingoDB.
 *
 *  BingoDB is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BingoDB is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with BingoDB.  If not, see <http://www.gnu.org/licenses/>.
 */
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
     * Serialization version field.
     */
    private static final long serialVersionUID = -3540471508460123577L;

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
