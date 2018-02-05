/* @ngInject */
function SearchReagentsController($rootScope, $uibModalInstance, notifyService, appValuesService,
                                  activeTabIndex, userReagentsService, searchService, searchUtil, addTableRowsCallback,
                                  searchReagentsService, stoichColumnActions, translateService, dictionaryService) {
    var vm = this;
    var myReagentsSearchQuery;

    init();

    function init() {
        vm.model = {
            restrictions: searchReagentsService.getRestrictions(),
            databases: searchService.getCatalogues()
        };
        vm.myReagents = {};
        vm.activeTabIndex = activeTabIndex;
        vm.isSearchResultFound = false;
        vm.conditionText = [{
            name: 'contains'
        }, {
            name: 'starts with'
        }, {
            name: 'ends with'
        }, {
            name: 'between'
        }];
        vm.conditionChemicalName = [{
            name: 'contains'
        }, {
            name: 'starts with'
        }, {
            name: 'ends with'
        }];
        vm.conditionNumber = [{
            name: '>'
        }, {
            name: '<'
        }, {
            name: '='
        }];
        vm.conditionSimilarity = [{
            name: 'equal'
        }, {
            name: 'substructure'
        }, {
            name: 'similarity'
        }];
        vm.chooseDBLabel = translateService.translate('CHOOSE_DBS_LABEL');
        vm.isAdvancedSearchCollapsed = true;
        vm.compoundStateModel = null;
        vm.compoundStatesList = [];

        // Getting list of coumpound states
        dictionaryService.getByName({
            name: vm.model.restrictions.advancedSearch.compoundState.name
        }, function(dictionary) {
            vm.compoundStatesList = dictionary.words;
        });

        myReagentsSearchQuery = '';

        vm.addToStoichTable = addToStoichTable;
        vm.addToMyReagentList = addToMyReagentList;
        vm.removeFromMyReagentList = removeFromMyReagentList;
        vm.isAdvancedSearchFilled = isAdvancedSearchFilled;
        vm.filterMyReagents = filterMyReagents;
        vm.clearMyReagentsSearchQuery = clearMyReagentsSearchQuery;
        vm.searchMyReagents = searchMyReagents;
        vm.onChangedStructure = onChangedStructure;
        vm.search = search;
        vm.cancel = cancel;
        vm.updateCompoundState = updateCompoundState;
    }

    function updateCompoundState() {
        vm.model.restrictions.advancedSearch.compoundState.value = vm.compoundStateModel
            ? vm.compoundStateModel.name : null;
    }

    function addToStoichTable(list) {
        var selected = _.filter(list, {
            $$isSelected: true
        });
        var count = 0;
        _.each(selected, function(selectedItem) {
            selectedItem.$$isSelected = false;
            selectedItem.$$isCollapsed = true;
            var cleanedReactants = stoichColumnActions.cleanReactants(selected);
            addTableRowsCallback(cleanedReactants);
            count += 1;
        });
        if (count > 0) {
            notifyService.info(count + ' reagents successfully added to Stoichiometry table');
        } else {
            notifyService.info('No reagents were chosen');
        }
    }

    userReagentsService.get({}, function(reagents) {
        vm.myReagentList = _.map(reagents, function(reagent) {
            reagent.$$isSelected = false;
            reagent.$$isCollapsed = true;
            reagent.rxnRole = reagent.rxnRole || appValuesService.getRxnRoleReactant();
            reagent.saltCode = reagent.saltCode || appValuesService.getDefaultSaltCode();

            return reagent;
        });
    });

    function addToMyReagentList() {
        var selected = _.filter(vm.searchResults, {
            $$isSelected: true
        });
        var count = 0;
        _.each(selected, function(selectedItem) {
            var isUnique = _.every(vm.myReagentList, function(myListItem) {
                return !_.isEqual(selectedItem, myListItem);
            });
            if (isUnique) {
                selectedItem.$$isCollapsed = true;
                vm.myReagentList.push(selectedItem);
                count += 1;
            }
        });
        if (count > 0) {
            userReagentsService.save(vm.myReagentList, function() {
                if (count === 1) {
                    notifyService.info(count + ' reagent successfully added to My Reagent List');
                } else if (count > 0) {
                    notifyService.info(count + ' reagents successfully added to My Reagent List');
                }
            });
        } else {
            notifyService.warning('My Reagent List already contains selected reagents');
        }
    }

    function removeFromMyReagentList() {
        var selected = _.filter(vm.myReagentList, {
            $$isSelected: true
        });
        _.each(selected, function(item) {
            vm.myReagentList = _.without(vm.myReagentList, item);
        });
        userReagentsService.save(vm.myReagentList);
    }

    function isAdvancedSearchFilled() {
        return searchUtil.isAdvancedSearchFilled(vm.model.restrictions.advancedSearch);
    }

    function responseCallback(result) {
        vm.searchResults = _.map(result, function(item) {
            var batchDetails = _.extend({}, item.details);
            batchDetails.$$isCollapsed = true;
            batchDetails.$$isSelected = false;
            batchDetails.nbkBatch = item.notebookBatchNumber;
            batchDetails.database = _.map(vm.model.databases, function(db) {
                return db.value;
            }).join(', ');
            batchDetails.rxnRole = batchDetails.rxnRole || appValuesService.getRxnRoleReactant();
            batchDetails.saltCode = batchDetails.saltCode || appValuesService.getDefaultSaltCode();

            return batchDetails;
        });
    }

    function filterMyReagents(reagent) {
        var query = myReagentsSearchQuery;
        if (_.isUndefined(query) || _.isNull(query) || query.trim().length === 0) {
            return true;
        }
        var regexp = new RegExp('.*' + query + '.*', 'i');
        if (reagent.compoundId && regexp.test(reagent.compoundId)) {
            return true;
        }
        if (reagent.chemicalName && regexp.test(reagent.chemicalName)) {
            return true;
        }
        if (reagent.formula && regexp.test(reagent.formula)) {
            return true;
        }

        return false;
    }

    function clearMyReagentsSearchQuery() {
        vm.myReagents.searchQuery = '';
        myReagentsSearchQuery = '';
    }

    function searchMyReagents(query) {
        myReagentsSearchQuery = query;
    }

    function search() {
        vm.loading = true;
        vm.searchResults = [];

        var searchRequest = searchUtil.prepareSearchRequest(vm.model.restrictions, vm.model.databases);

        vm.model.restrictions.advancedSummary = searchRequest.advancedSearch;
        searchService.search(searchRequest).$promise
            .then(function(result) {
                responseCallback(result);
                vm.loading = false;
            });

        vm.isSearchResultFound = true;

        // Expand advanced search form if it has any restrictions set
        vm.isAdvancedSearchCollapsed = _.isEmpty(vm.model.restrictions.advancedSummary);
    }

    function cancel() {
        $uibModalInstance.close({});
    }

    function onChangedStructure(structure) {
        _.extend(vm.model.restrictions.structure, structure);
    }
}

module.exports = SearchReagentsController;
