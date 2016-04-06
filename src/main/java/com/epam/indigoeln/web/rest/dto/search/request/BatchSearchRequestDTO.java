package com.epam.indigoeln.web.rest.dto.search.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public class BatchSearchRequestDTO {

    private Optional<String> searchQuery;
    @JsonProperty("criteria")
    private List<BatchSearchCriteriaDTO> criteriaList;
    private Optional<BatchSearchStructureDTO> structure;
    private List<String> databases;

    public Optional<String> getSearchQuery() {
        return searchQuery;
    }

    public List<BatchSearchCriteriaDTO> getCriteriaList() {
        return criteriaList;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public Optional<BatchSearchStructureDTO> getStructure() {
        return structure;
    }

    public void setSearchQuery(Optional<String> searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setCriteriaList(List<BatchSearchCriteriaDTO> criteriaList) {
        this.criteriaList = criteriaList;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public void setStructure(Optional<BatchSearchStructureDTO> structure) {
        this.structure = structure;
    }
}
