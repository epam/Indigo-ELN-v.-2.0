package com.epam.indigoeln.core.service.search.impl.pubchem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class CidsDTO {

    private List<String> cids;

    public List<String> getCids() {
        return cids;
    }

    @JsonProperty("IdentifierList")
    public void setCids(Map<String, List<String>> identifierList) {
        this.cids = identifierList.get("CID");
    }
}
