angular.module('indigoeln')
    .factory('SearchUtilService', searchUtilService);

/* @ngInject */
function searchUtilService() {

    return {
        prepareSearchRequest: prepareSearchRequest,
        isAdvancedSearchFilled: isAdvancedSearchFilled

    };

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
        var advancedSearch = restrictions.advancedSearch;
        var advancedSummary = restrictions.advancedSummary = [];
        _.each(advancedSearch, function (restriction) {
            if (restriction.value) {
                var restrictionCopy = angular.copy(restriction);
                if (restrictionCopy.condition) {
                    restrictionCopy.condition = restrictionCopy.condition.name;
                } else {
                    restrictionCopy.condition = '';
                }
                if (restrictionCopy.getValue) {
                    restrictionCopy.value = restrictionCopy.getValue(restrictionCopy.value);
                }
                advancedSummary.push(restrictionCopy);
            }
        });
        return advancedSummary.length ? advancedSummary : null;
    }

    function prepareDatabases(databases) {
        return _.pluck(_.where(databases, {isChecked: true}), 'value');
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
        return !!_.compact(_.map(advancedSearch, function (restriction) {
            return restriction.value;
        })).length;
    }
}