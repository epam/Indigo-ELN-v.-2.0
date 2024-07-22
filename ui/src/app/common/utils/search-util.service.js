/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function searchUtil(modelRestrictions) {
    var storedOptions;

    return {
        prepareSearchRequest: prepareSearchRequest,
        isAdvancedSearchFilled: isAdvancedSearchFilled,
        getStoredModel: getStoredModel,
        getStoredOptions: getStoredOptions
    };

    function getStoredOptions() {
        if (storedOptions) {
            return storedOptions;
        }
        storedOptions = {
            isCollapsed: true
        };

        return storedOptions;
    }

    function getStoredModel() {
        return angular.copy(modelRestrictions);
    }

    function prepareStructure(restrictions) {
        if (!restrictions.structure.molfile) {
            return null;
        }
        var structure = restrictions.structure;
        var searchMode = restrictions.structure.similarityCriteria.name;
        if (searchMode === 'equal') {
            searchMode = 'exact';
        }
        structure.searchMode = searchMode;
        structure.similarity = restrictions.structure.similarityValue / 100;

        return structure;
    }

    function prepareAdvancedSearch(restrictions) {
        var advancedSummary = [];

        _.each(restrictions.advancedSearch, function(restriction) {
            restriction.value = restriction.selectedValue
                ? restriction.selectedValue.name
                : restriction.value;

            var hasValue = _.isArray(restriction.value) && restriction.value.length === 0
                ? false
                : restriction.value;

            if (hasValue) {
                advancedSummary.push({
                    field: restriction.field,
                    value: restriction.value,
                    condition: restriction.condition ? restriction.condition.name : ''
                });
            }
        });

        return advancedSummary.length ? advancedSummary : null;
    }

    function prepareDatabases(databases) {
        return _.map(_.filter(databases, {
            isChecked: true
        }), 'value');
    }

    function prepareSearchRequest(restrictions, databases) {
        var searchRequest = {};
        if (restrictions.searchQuery) {
            searchRequest.searchQuery = restrictions.searchQuery;
        }

        var advancedSearch = prepareAdvancedSearch(restrictions);
        if (advancedSearch) {
            searchRequest.advancedSearch = advancedSearch;
        }

        var structure = prepareStructure(restrictions);
        if (structure) {
            searchRequest.structure = structure;
        }
        searchRequest.databases = prepareDatabases(databases);

        return searchRequest;
    }

    function isAdvancedSearchFilled(advancedSearch) {
        return !!_.compact(_.map(advancedSearch, function(restriction) {
            return restriction.value;
        })).length;
    }
}

module.exports = searchUtil;
