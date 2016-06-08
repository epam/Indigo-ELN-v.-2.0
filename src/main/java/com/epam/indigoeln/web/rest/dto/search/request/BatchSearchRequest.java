package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BatchSearchRequest {

    private Optional<String> searchQuery = Optional.empty();
    private Optional<BatchSearchStructure> structure = Optional.empty();
    private List<BatchSearchCriteria> advancedSearch = Collections.emptyList();
    private List<String> databases = Collections.emptyList();

    public Optional<String> getSearchQuery() {
        return searchQuery;
    }

    public List<BatchSearchCriteria> getAdvancedSearch() {
        return advancedSearch;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public Optional<BatchSearchStructure> getStructure() {
        return structure;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = Optional.ofNullable(searchQuery);
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases != null ? databases : Collections.emptyList();
    }

    public void setStructure(BatchSearchStructure structure) {
        this.structure = Optional.ofNullable(structure);
    }

    public void setAdvancedSearch(List<BatchSearchCriteria> advancedSearch) {
        this.advancedSearch = advancedSearch != null ? advancedSearch : Collections.emptyList();
    }
}
