package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.User;

import java.io.Serializable;

/**
 * Short transfer object for User
 */
public class ShortUserDTO {

    private static final long serialVersionUID = 1522754620900492959L;

    private String id;
    private String firstName;
    private String lastName;
    private String email;

    public ShortUserDTO() {
        super();
    }

    public ShortUserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "ShortUserDTO{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
