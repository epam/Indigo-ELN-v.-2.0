package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BatchSearchRequest {

    private Optional<String> searchQuery = Optional.empty();
    private Optional<BatchSearchStructure> structure = Optional.empty();
    private List<SearchCriterion> advancedSearch = Collections.emptyList();
    private List<String> databases = Collections.emptyList();

    public Optional<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = Optional.ofNullable(searchQuery);
    }

    public List<SearchCriterion> getAdvancedSearch() {
        return advancedSearch;
    }

    public void setAdvancedSearch(List<SearchCriterion> advancedSearch) {
        this.advancedSearch = advancedSearch != null ? advancedSearch : Collections.emptyList();
    }

    public List<String> getDatabases() {
        return databases;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases != null ? databases : Collections.emptyList();
    }

    public Optional<BatchSearchStructure> getStructure() {
        return structure;
    }

    public void setStructure(BatchSearchStructure structure) {
        this.structure = Optional.ofNullable(structure);
    }
}
