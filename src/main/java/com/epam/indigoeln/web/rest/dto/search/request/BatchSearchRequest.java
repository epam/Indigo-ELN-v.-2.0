package com.epam.indigoeln.web.rest.dto.search.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public class BatchSearchRequest {

    private Optional<String> searchQuery;
    @JsonProperty("criteria")
    private List<BatchSearchCriteria> criteriaList;
    private Optional<BatchSearchStructure> structure;
    private List<String> databases;

    public Optional<String> getSearchQuery() {
        return searchQuery;
    }

    public List<BatchSearchCriteria> getCriteriaList() {
        return criteriaList;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public Optional<BatchSearchStructure> getStructure() {
        return structure;
    }

    public void setSearchQuery(Optional<String> searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setCriteriaList(List<BatchSearchCriteria> criteriaList) {
        this.criteriaList = criteriaList;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public void setStructure(Optional<BatchSearchStructure> structure) {
        this.structure = structure;
    }
}
