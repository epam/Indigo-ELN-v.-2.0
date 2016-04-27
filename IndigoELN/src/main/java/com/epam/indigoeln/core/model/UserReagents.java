package com.epam.indigoeln.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.mongodb.BasicDBList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = UserReagents.COLLECTION_NAME)
public class UserReagents {

    public static final String COLLECTION_NAME = "user_reagents";

    @Id
    private String id;

    private User user;

    private BasicDBList reagents;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BasicDBList getReagents() {
        return reagents;
    }

    public void setReagents(BasicDBList reagents) {
        this.reagents = reagents;
    }
}