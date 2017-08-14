angular
    .module('indigoeln')
    .controller('SearchReagentsController', SearchReagentsController);

/* @ngInject */
function SearchReagentsController($rootScope, $uibModalInstance, notifyService, AppValues, activeTab, UserReagents,
                                  SearchService, SearchUtilService, searchReagentsConstant, stoichHelper) {
    var vm = this;
    var myReagentsSearchQuery;

    init();

    function init() {
        vm.model = {};
        vm.model.restrictions = searchReagentsConstant.restrictions;
        vm.model.databases = SearchService.getCatalogues();
        vm.myReagents = {};

        vm.isActiveTab0 = activeTab === 0;
        vm.isActiveTab1 = activeTab === 1;
        vm.searchReagentsModelScheme = {};
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

        myReagentsSearchQuery = '';

        vm.addToStoichTable = addToStoichTable;
        vm.addToMyReagentList = addToMyReagentList;
        vm.removeFromMyReagentList = removeFromMyReagentList;
        vm.isAdvancedSearchFilled = isAdvancedSearchFilled;
        vm.filterMyReagents = filterMyReagents;
        vm.clearMyReagentsSearchQuery = clearMyReagentsSearchQuery;
        vm.searchMyReagents = searchMyReagents;
        vm.search = search;
        vm.cancel = cancel;
    }

    function addToStoichTable(list) {
        var selected = _.filter(list, {
            $$isSelected: true
        });
        $rootScope.$broadcast('stoich-rows-changed', stoichHelper.cleanReactants(selected));
    }

    UserReagents.get({}, function(reagents) {
        vm.myReagentList = _.map(reagents, function(reagent) {
            reagent.$$isSelected = false;
            reagent.$$isCollapsed = true;
            reagent.rxnRole = reagent.rxnRole || AppValues.getRxnRoleReactant();
            reagent.saltCode = reagent.saltCode || AppValues.getDefaultSaltCode();

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
                selectedItem.$$isSelected = false;
                selectedItem.$$isCollapsed = true;
                vm.myReagentList.push(selectedItem);
                count += 1;
            }
        });
        if (count > 0) {
            UserReagents.save(vm.myReagentList, function() {
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
        UserReagents.save(vm.myReagentList);
    }

    function isAdvancedSearchFilled() {
        return SearchUtilService.isAdvancedSearchFilled(vm.model.restrictions.advancedSearch);
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
            batchDetails.rxnRole = batchDetails.rxnRole || AppValues.getRxnRoleReactant();
            batchDetails.saltCode = batchDetails.saltCode || AppValues.getDefaultSaltCode();

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
        var searchRequest = SearchUtilService.prepareSearchRequest(vm.model.restrictions, vm.model.databases);
        SearchService.search(searchRequest, function(result) {
            responseCallback(result);
            vm.loading = false;
        });
        vm.isSearchResultFound = true;
    }

    function cancel() {
        $uibModalInstance.close({});
    }
}
