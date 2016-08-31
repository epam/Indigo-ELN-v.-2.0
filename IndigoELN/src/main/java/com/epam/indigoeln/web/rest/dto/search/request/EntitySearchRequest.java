package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EntitySearchRequest {

    private Optional<String> searchQuery = Optional.empty();

    private Optional<String> searchKind = Optional.empty();

    private List<SearchCriterion> advancedSearch = Collections.emptyList();

    public Optional<String> getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = Optional.ofNullable(searchQuery);
    }

    public Optional<String> getSearchKind() {
        return searchKind;
    }

    public void setSearchKind(String searchKind) {
        this.searchKind = Optional.ofNullable(searchKind);
    }

    public List<SearchCriterion> getAdvancedSearch() {
        return advancedSearch;
    }

    public void setAdvancedSearch(List<SearchCriterion> advancedSearch) {
        this.advancedSearch = advancedSearch;
    }
}
