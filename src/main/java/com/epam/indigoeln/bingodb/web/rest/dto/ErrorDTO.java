package com.epam.indigoeln.bingodb.web.rest.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Simple DTO for REST errors
 */
public class ErrorDTO implements Serializable {

    /**
     * Error message
     */
    private String message;

    /**
     * Create a new ErrorDTO instance
     */
    public ErrorDTO() {
        // Empty constructor for JSON deserialize
    }

    /**
     * Create a new ErrorDTO instance
     * @param message an error message
     */
    public ErrorDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

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
