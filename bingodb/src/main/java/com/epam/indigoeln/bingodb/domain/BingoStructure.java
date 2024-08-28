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
package com.epam.indigoeln.bingodb.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Entity for holding structure with its ID.
 */
public class BingoStructure implements Serializable {

    /**
     * Serialization version field.
     */
    private static final long serialVersionUID = 2900530361336251030L;

    /**
     * Structure ID.
     */
    private String id;

    /**
     * Structure.
     */
    private String structure;

    /**
     * Create a new BingoStructure instance.
     */
    public BingoStructure() {
        // Empty constructor for JSON deserialize
    }

    /**
     * Create a new BingoStructure instance.
     *
     * @param id        structure id
     * @param structure structure
     */
    public BingoStructure(String id, String structure) {
        this.id = id;
        this.structure = structure;
    }

    /**
     * Get an existing structure id value.
     *
     * @return existing structure id value
     */
    public String getId() {
        return id;
    }

    /**
     * Set a new structure id value.
     *
     * @param id new structure id value to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get an existing list of structures.
     *
     * @return existing list of structures
     */
    public String getStructure() {
        return structure;
    }

    /**
     * Set a new list of structures.
     *
     * @param structure new list of structures to set
     */
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
