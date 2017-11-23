package com.epam.indigoeln.core.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Component model object
 */
@Data
@Accessors(chain = true)
@Document(collection = Component.COLLECTION_NAME)
public class Component implements Serializable {

    public static final String COLLECTION_NAME = "component";

    private static final long serialVersionUID = -3522699714105273016L;

    @Id
    private String id;
    private String name;
    private BasicDBObject content;

    private DBRef experiment;

    @Version
    private Long version;
}
