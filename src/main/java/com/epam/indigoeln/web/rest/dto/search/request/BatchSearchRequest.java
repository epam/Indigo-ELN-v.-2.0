/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.web.rest.dto.search.request;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BatchSearchRequest {

    private Optional<String> searchQuery = Optional.empty();
    private Optional<BatchSearchStructure> structure = Optional.empty();
    private List<SearchCriterion> advancedSearch = Collections.emptyList();
    private List<String> databases = Collections.emptyList();
    private Optional<Integer> batchesLimit = Optional.empty();

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

    public Optional<Integer> getBatchesLimit() {
        return batchesLimit;
    }

    public void setBatchesLimit(Integer batchesLimit) {
        this.batchesLimit = Optional.ofNullable(batchesLimit);
    }
}
