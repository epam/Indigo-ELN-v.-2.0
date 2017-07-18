angular
    .module('indigoeln')
    .controller('analyzeRxnController', analyzeRxnController);

/* @ngInject */
function analyzeRxnController($uibModalInstance, reactants, SearchService, AppValues, onStoichRowsChanged, stoihHelper) {
    var vm = this;

    vm.addToStoichTable = addToStoichTable;
    vm.updateStoicAndExit = updateStoicAndExit;
    vm.search = search;
    vm.cancel = cancel;

    init();

    function init() {
        vm.model = getDefaultModel();
        vm.reactants = reactants;

        vm.tabs = _.map(vm.reactants, function(reactant) {
            return {
                formula: reactant.formula, searchResult: [], selectedReactant: null
            };
        });
    }

    function addToStoichTable() {
        onStoichRowsChanged(stoihHelper.cleanReactants(vm.model.selectedReactants));
    }

    function updateStoicAndExit() {
        var result = angular.copy(vm.reactants);
        _.each(vm.model.selectedReactants, function(knownReactant) {
            _.extend(_.find(result, {
                formula: knownReactant.formula
            }), knownReactant);
        });
        onStoichRowsChanged(stoihHelper.cleanReactants(result));
        $uibModalInstance.close({});
    }

    function search() {
        vm.loading = true;
        _.each(vm.tabs, function(tab) {
            getSearchResult(tab.formula, function(searchResult) {
                vm.loading = false;
                tab.searchResult = responseCallback(searchResult);
            });
        });
        vm.isSearchCompleted = true;
    }

    function cancel() {
        $uibModalInstance.close({});
    }

    function getDefaultModel() {
        return {
            reactants: reactants,
            selectedReactants: [],
            isSearchResultFound: false,
            databases: SearchService.getCatalogues()
        };
    }

    function prepareDatabases() {
        return _.map(_.filter(vm.model.databases, {
            isChecked: true
        }), 'value');
    }

    function responseCallback(result) {
        var databases = prepareDatabases();

        return _.map(result, function(item) {
            var batchDetails = _.extend({}, item.details);
            batchDetails.$$isCollapsed = true;
            batchDetails.$$isSelected = false;
            batchDetails.nbkBatch = item.notebookBatchNumber;
            batchDetails.database = databases.join(', ');
            batchDetails.rxnRole = batchDetails.rxnRole || AppValues.getRxnRoleReactant();
            batchDetails.saltCode = batchDetails.saltCode || AppValues.getDefaultSaltCode();

            return batchDetails;
        });
    }

    function getSearchResult(formula, callback) {
        var databases = prepareDatabases();
        var searchRequest = {
            databases: databases,
            structure: {
                formula: formula, searchMode: 'molformula'
            }
        };
        SearchService.search(searchRequest, callback);
    }
}
