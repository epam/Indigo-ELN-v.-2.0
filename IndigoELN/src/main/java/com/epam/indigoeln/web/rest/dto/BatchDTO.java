package com.epam.indigoeln.web.rest.dto;

import java.io.Serializable;
import com.epam.indigoeln.core.model.Batch;

public class BatchDTO implements Serializable {

    private static final long serialVersionUID = 13994253258215137L;

    private String id;
    private String batchNumber;
    private String jsonContent;
    private String molfile;
    private String componentTemplateId;

    public BatchDTO() {
    }

    public BatchDTO(Batch batch) {
        this(batch.getId(),
             batch.getBatchNumber(),
             batch.getJsonContent(),
             batch.getComponentTemplateId());
    }

    public BatchDTO(String id,
                    String batchNumber,
                    String jsonContent,
                    String componentTemplateId) {
        this.id = id;
        this.batchNumber = batchNumber;
        this.jsonContent = jsonContent;
        this.componentTemplateId = componentTemplateId;
    }

    public String getId() {
        return id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public String getMolfile() {
        return molfile;
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

    public void setMolfile(String molfile) {
        this.molfile = molfile;
    }

    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }

    public void setComponentTemplateId(String componentTemplateId) {
        this.componentTemplateId = componentTemplateId;
    }

    @Override
    public String toString() {
        return "BatchDTO{" +
                "id='" + id + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", jsonContent='" + jsonContent + '\'' +
                ", molfile='" + molfile + '\'' +
                ", componentTemplateId='" + componentTemplateId + '\'' +
                '}';
    }
}
