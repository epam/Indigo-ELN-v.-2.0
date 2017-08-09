package com.epam.indigoeln.core.model;

import com.mongodb.BasicDBObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Document(collection = AutosaveItem.COLLECTION_NAME)
public class AutosaveItem implements Serializable, Persistable<String> {

    public static final String COLLECTION_NAME = "autosave";
    private static final long serialVersionUID = 5299632261167812262L;

    @Id
    private String id;

    private User user;

    private BasicDBObject content;

    public AutosaveItem() {
    }

    public AutosaveItem(String id, User user, BasicDBObject content) {
        this.id = id;
        this.user = user;
        this.content = content;
    }

    @Override
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

    public BasicDBObject getContent() {
        return content;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
