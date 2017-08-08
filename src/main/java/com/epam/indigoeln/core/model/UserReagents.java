package com.epam.indigoeln.core.model;

import com.mongodb.BasicDBList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Document(collection = UserReagents.COLLECTION_NAME)
public class UserReagents implements Serializable {

    public static final String COLLECTION_NAME = "user_reagents";
    private static final long serialVersionUID = 525366059291530286L;

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