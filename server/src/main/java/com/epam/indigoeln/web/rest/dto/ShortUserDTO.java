/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.dto;

import com.epam.indigoeln.core.model.User;
import com.mongodb.DBObject;
import org.bson.Document;

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

    ShortUserDTO(Document user) {
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
