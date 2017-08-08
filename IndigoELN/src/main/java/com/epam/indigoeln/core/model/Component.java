package com.epam.indigoeln.core.model;

import com.mongodb.BasicDBObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Component model object
 */
@ToString
@EqualsAndHashCode
@Document(collection = Component.COLLECTION_NAME)
public class Component implements Serializable {

    public static final String COLLECTION_NAME = "component";

    private static final long serialVersionUID = -3522699714105273016L;

    @Id
    private String id;
    private String name;
    private BasicDBObject content;

    @Version
    private Long version;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
