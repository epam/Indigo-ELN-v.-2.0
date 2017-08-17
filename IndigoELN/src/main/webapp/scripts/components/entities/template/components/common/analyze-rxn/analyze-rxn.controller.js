angular
    .module('indigoeln')
    .controller('analyzeRxnController', analyzeRxnController);

/* @ngInject */
function analyzeRxnController($uibModalInstance, reactants, SearchService, AppValues, onStoichRowsChanged, stoichHelper,
                              $q) {
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
        onStoichRowsChanged(stoichHelper.cleanReactants(vm.model.selectedReactants));
    }

    function updateStoicAndExit() {
        var result = angular.copy(vm.reactants);
        _.each(vm.model.selectedReactants, function(knownReactant) {
            _.extend(_.find(result, {
                formula: knownReactant.formula
            }), knownReactant);
        });
        onStoichRowsChanged(stoichHelper.cleanReactants(result));
        $uibModalInstance.close({});
    }

    function search() {
        vm.loading = true;
        $q.all(_.map(vm.tabs, function(tab) {
            return getSearchResult(tab.formula).then(function(searchResult) {
                tab.searchResult = responseCallback(searchResult);
            });
        })).finally(function() {
            vm.loading = false;
            vm.isSearchCompleted = true;
        });
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

    function getSearchResult(formula) {
        var databases = prepareDatabases();
        var searchRequest = {
            databases: databases,
            structure: {
                formula: formula, searchMode: 'molformula'
            }
        };

        return SearchService.search(searchRequest).$promise;
    }
}
