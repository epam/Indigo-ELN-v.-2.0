package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.User;
import com.mongodb.DBObject;

/**
 * Short transfer object for User.
 */
public class ShortUserDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String email;

    public ShortUserDTO() {
        super();
    }

    ShortUserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
    }

    ShortUserDTO(DBObject user) {
        this.id = String.valueOf(user.get("_id"));
        this.firstName = String.valueOf(user.get("first_name"));
        this.lastName = String.valueOf(user.get("last_name"));
        this.email = String.valueOf(user.get("email"));
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
        return "ShortUserDTO{"
                + "id='" + id + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
