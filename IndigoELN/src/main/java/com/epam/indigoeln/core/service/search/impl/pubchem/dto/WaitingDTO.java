package com.epam.indigoeln.core.service.search.impl.pubchem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class WaitingDTO {

    private String listKey;

    public String getListKey() {
        return listKey;
    }

    @JsonProperty("Waiting")
    public void setListKey(Map<String, String> waiting) {
        this.listKey = waiting.get("ListKey");
    }
}
