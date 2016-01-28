package com.epam.indigoeln.core.model;

import com.google.common.base.Objects;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * Batch model object
 */
public class Batch implements Serializable {

    private static final long serialVersionUID = -3522699714105273016L;

    @NotNull
    @Pattern(regexp = "^[a-z0-9]*")
    private String id;

    @NotBlank
    private String batchNumber;

    private Integer bingoDbId;

    private String jsonContent;

    @NotNull
    @Pattern(regexp = "^[a-z0-9]*")
    private String componentTemplateId;

    public String getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
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

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
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
        Batch batch = (Batch) o;
        return Objects.equal(id, batch.id) &&
               Objects.equal(batchNumber, batch.batchNumber) &&
               Objects.equal(bingoDbId, batch.bingoDbId) &&
               Objects.equal(jsonContent, batch.jsonContent) &&
               Objects.equal(componentTemplateId, batch.componentTemplateId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, batchNumber, bingoDbId, jsonContent, componentTemplateId);
    }
}
