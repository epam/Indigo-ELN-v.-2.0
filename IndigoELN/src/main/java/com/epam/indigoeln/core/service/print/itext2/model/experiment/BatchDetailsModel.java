package com.epam.indigoeln.core.service.print.itext2.model.experiment;

import com.epam.indigoeln.core.service.print.itext2.model.SectionModel;
import com.mongodb.BasicDBObject;

import java.util.LinkedHashMap;

public class BatchDetailsModel implements SectionModel {
    private LinkedHashMap<String, String> data;
    private String fullNbkBatch;

    public BatchDetailsModel(BasicDBObject content) {
        fullNbkBatch = content.getString("fullNbkBatch");
        data = new LinkedHashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        data.put("TODO...", "TODO...");
    }

    public LinkedHashMap<String, String> getData() {
        return data;
    }

    public String getFullNbkBatch() {
        return fullNbkBatch;
    }
}
