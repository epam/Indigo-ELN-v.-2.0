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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Simple DTO for REST errors.
 */
public class ErrorDTO implements Serializable {

    /**
     * Serialization version field.
     */
    private static final long serialVersionUID = -5553970958183749770L;

    /**
     * Error message.
     */
    private String message;

    /**
     * Create a new ErrorDTO instance.
     */
    public ErrorDTO() {
        // Empty constructor for JSON deserialize
    }

    /**
     * Create a new ErrorDTO instance.
     *
     * @param message an error message
     */
    public ErrorDTO(String message) {
        this.message = message;
    }

    /**
     * Get an existing message.
     *
     * @return existing message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set a new message.
     *
     * @param message new message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ErrorDTO errorDTO = (ErrorDTO) o;

        return new EqualsBuilder()
                .append(message, errorDTO.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .toString();
    }
}
