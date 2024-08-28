/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.entity;

import com.chemistry.enotebook.signature.Util;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Map;
import java.util.UUID;

public class User extends JsonRepresentable {
    public static final String USER_ID = "userid";
    public static final String USERNAME = "username";
    public static final String FIRST_NAME = "firstname";
    public static final String LAST_NAME = "lastname";
    public static final String EMAIL = "email";
    public static final String ADMIN = "admin";
    public static final String ACTIVE = "active";
    public static final String CREATION_DATE = "creationdate";
    public static final String CREATED_BY = "createdby";
    public static final String PASSWORD = "password";

    private UUID userId;
    private String username = "";
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private Boolean admin;
    private Boolean active;
    private long creationDate;
    private String createdBy;
    private String password;

    public User() {
        creationDate = System.currentTimeMillis();
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof User) {
            User user = (User)o;
            return this.getUserId().equals(user.getUserId());
        }
        return false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static User generateUserFromJson(JsonObject row) {
        User user = new User();
        user.setUserId(getUuid(row, USER_ID));
        user.setUsername(getString(row, USERNAME));
        user.setFirstName(getString(row, FIRST_NAME));
        user.setLastName(getString(row, LAST_NAME));
        user.setEmail(getString(row, EMAIL));
        user.setAdmin(getBoolean(row, ADMIN));
        user.setActive(getBoolean(row, ACTIVE));
        user.setCreationDate(row.containsKey(CREATION_DATE) ? Util.dateLongFromObject(row.getString(CREATION_DATE)): System.currentTimeMillis());
        user.setCreatedBy(getString(row, CREATED_BY));
        user.setPassword(getMd5Password(row));

        return user;
    }

    public static User generateUser(Map row) {
        User user = new User();
        user.setUserId(Util.uuidFromObject(row.get(USER_ID)));
        user.setUsername((String)row.get(USERNAME));
        user.setFirstName((String)row.get(FIRST_NAME));
        user.setLastName((String)row.get(LAST_NAME));
        user.setEmail((String)row.get(EMAIL));
        user.setAdmin((Boolean)row.get(ADMIN));
        user.setActive((Boolean)row.get(ACTIVE));
        user.setCreationDate(Util.dateLongFromObject(row.get(CREATION_DATE)));
        user.setCreatedBy((String)row.get(CREATED_BY));
        user.setPassword((String)row.get(PASSWORD));

        return user;
    }

    @Override
    public JsonObject asJson() {
        JsonObject user = Json.createObjectBuilder()
                .add(USER_ID,  userId.toString())
                .add(USERNAME, username)
                .add(FIRST_NAME, firstName)
                .add(LAST_NAME, lastName)
                .add(EMAIL, email)
                .add(ADMIN, admin)
                .add(ACTIVE, active)
                .build();
        return user;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
