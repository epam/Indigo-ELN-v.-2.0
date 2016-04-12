package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.List;

public class BatchSearchRequest {

    private String searchQuery;
    private List<BatchSearchCriteria> advancedSearch;
    private BatchSearchStructure structure;
    private List<String> databases;

    public String getSearchQuery() {
        return searchQuery;
    }

    public List<BatchSearchCriteria> getAdvancedSearch() {
        return advancedSearch;
    }
    public List<String> getDatabases() {
        return databases;
    }

    public BatchSearchStructure getStructure() {
        return structure;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public void setStructure(BatchSearchStructure structure) {
        this.structure = structure;
    }

    public void setAdvancedSearch(List<BatchSearchCriteria> advancedSearch) {
        this.advancedSearch = advancedSearch;
    }
}
