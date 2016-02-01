package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotBlank;
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

    @NotNull
    @Pattern(regexp = "^[a-z0-9]*")
    private String id;

    @NotBlank
    private String componentNumber;

    private Integer bingoDbId;

    private String jsonContent;

    @NotNull
    @Pattern(regexp = "^[a-z0-9]*")
    private String componentTemplateId;

    public String getId() {
        return id;
    }

    public String getComponentNumber() {
        return componentNumber;
    }

    public Integer getBingoDbId() {
        return bingoDbId;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    public String getComponentTemplateId() {
        return componentTemplateId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setComponentNumber(String componentNumber) {
        this.componentNumber = componentNumber;
    }

    public void setBingoDbId(Integer bingoDbId) {
        this.bingoDbId = bingoDbId;
    }

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
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
               Objects.equal(componentNumber, component.componentNumber) &&
               Objects.equal(bingoDbId, component.bingoDbId) &&
               Objects.equal(jsonContent, component.jsonContent) &&
               Objects.equal(componentTemplateId, component.componentTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, componentNumber, bingoDbId, jsonContent, componentTemplateId);
    }
}
