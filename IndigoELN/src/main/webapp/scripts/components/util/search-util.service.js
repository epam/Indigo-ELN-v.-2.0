angular.module('indigoeln')
    .factory('SearchUtilService', searchUtilService);

/* @ngInject */
function searchUtilService() {
    var storedModel,
        storedOptions;

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

    function getStoredModel(clear) {
        if (storedModel && !clear) {
            return storedModel;
        }
        storedModel = {};
        // TODO: move to file
        storedModel.restrictions = {
            searchQuery: '',
            advancedSearch: {
                therapeuticArea: {
                    name: 'Therapeutic Area',
                    field: 'therapeuticArea',
                    condition: {name: 'contains'},
                    $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                projectCode: {
                    name: 'Project Code', field: 'projectCode', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                batchYield: {
                    name: 'Batch Yield%', field: 'batchYield', condition: {name: '>'}, $$conditionList: [
                        {name: '>'}, {name: '<'}, {name: '='}, {name: '~'}
                    ]
                },
                batchPurity: {
                    name: 'Batch Purity%', field: 'purity', condition: {name: '>'}, $$conditionList: [
                        {name: '>'}, {name: '<'}, {name: '='}, {name: '~'}
                    ]
                },
                subject: {
                    name: 'Subject/Title', field: 'name', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                entityDescription: {
                    name: 'Entity Description', field: 'description', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                compoundId: {
                    name: 'Compound ID', field: 'compoundId', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                literatureRef: {
                    name: 'Literature Ref', field: 'references', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                entityKeywords: {
                    name: 'Entity Keywords', field: 'keywords', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                chemicalName: {
                    name: 'Chemical Name', field: 'chemicalName', condition: {name: 'contains'}, $$conditionList: [
                        {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                    ]
                },
                entityTypeCriteria: {
                    name: 'Entity Type Criteria',
                    $$skipList: true,
                    field: 'kind',
                    condition: {
                        name: 'in'
                    },
                    value: []
                },

                entityDomain: {
                    name: 'Entity Searching Domain',
                    $$skipList: true,
                    field: 'author._id',
                    condition: {
                        name: 'in'
                    },
                    value: []
                },

                statusCriteria: {
                    name: 'Status',
                    $$skipList: true,
                    field: 'status',
                    condition: {
                        name: 'in'
                    },
                    value: []
                }
            },
            users: [],
            entityType: 'Project',
            structure: {
                name: 'Reaction Scheme',
                similarityCriteria: {
                    name: 'equal'
                },
                similarityValue: null,
                image: null,
                type: {
                    name: 'Product'
                }
            }
        };

        return storedModel;
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
        var advancedSummary = restrictions.advancedSummary = [];
        _.each(advancedSearch, function(restriction) {
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
                restrictionCopy.someField = 'Value that means nothing'
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
