package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EntitySearchRequest {

    private Optional<String> searchQuery = Optional.empty();

    private List<SearchCriterion> advancedSearch = Collections.emptyList();

    private Optional<EntitySearchStructure> structure = Optional.empty();

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
        this.advancedSearch = advancedSearch;
    }

    public Optional<EntitySearchStructure> getStructure() {
        return structure;
    }

    public void setStructure(EntitySearchStructure structure) {
        this.structure = Optional.ofNullable(structure);
    }
}
