package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import com.mongodb.BasicDBObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Component model object
 */
@Document(collection = Component.COLLECTION_NAME)
public class Component implements Serializable {
    public static final String COLLECTION_NAME = "component";

    private static final long serialVersionUID = -3522699714105273016L;

    @Id
    private String id;
    private BasicDBObject content;

    public String getId() {
        return id;
    }

    public BasicDBObject getContent() {
        return content;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return Objects.equal(id, component.id) &&
               Objects.equal(content, component.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, content);
    }
}
