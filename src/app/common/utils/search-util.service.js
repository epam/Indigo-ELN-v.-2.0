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
        var advancedSearch = restrictions.advancedSearch;
        var advancedSummary = [];
        restrictions.advancedSummary = advancedSummary;

        _.each(advancedSearch, function(restriction) {
            restriction.value = _.isUndefined(restriction.selectedValue)
                ? restriction.value
                : restriction.selectedValue.name;

            var value = _.isArray(restriction.value) && restriction.value.length === 0 ? null : restriction.value;
            if (value) {
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
