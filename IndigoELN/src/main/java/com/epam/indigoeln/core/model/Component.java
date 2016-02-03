package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import com.mongodb.BasicDBObject;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * Component model object
 */
@Document(collection = "component")
public class Component implements Serializable {

    private static final long serialVersionUID = -3522699714105273016L;

    @Id
    private String id;

    private Integer bingoDbId;

    private BasicDBObject content;

    @NotNull
    @Pattern(regexp = "^[a-z0-9]*")
    private String componentTemplateId;

    public String getId() {
        return id;
    }

    public Integer getBingoDbId() {
        return bingoDbId;
    }

    public BasicDBObject getContent() {
        return content;
    }

    public String getComponentTemplateId() {
        return componentTemplateId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBingoDbId(Integer bingoDbId) {
        this.bingoDbId = bingoDbId;
    }

    public void setContent(BasicDBObject content) {
        this.content = content;
    }

    public void setComponentTemplateId(String componentTemplateId) {
        this.componentTemplateId = componentTemplateId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return Objects.equal(id, component.id) &&
               Objects.equal(bingoDbId, component.bingoDbId) &&
               Objects.equal(content, component.content) &&
               Objects.equal(componentTemplateId, component.componentTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, bingoDbId, content, componentTemplateId);
    }
}
