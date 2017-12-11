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
                var requestRestriction = {
                    field: restriction.field,
                    value: restriction.value
                };

                if (restriction.condition) {
                    requestRestriction.condition = restriction.condition.name;
                } else {
                    requestRestriction.condition = '';
                }

                if (restriction.getValue) {
                    requestRestriction.value = restriction.getValue(restriction.value);
                }

                advancedSummary.push(requestRestriction);
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
