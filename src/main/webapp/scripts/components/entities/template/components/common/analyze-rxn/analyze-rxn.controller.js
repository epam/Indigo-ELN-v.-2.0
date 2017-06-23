angular
    .module('indigoeln')
    .controller('analyzeRxnController', analyzeRxnController);

/* @ngInject */
function analyzeRxnController($scope, $uibModalInstance, reactants, SearchService, AppValues, onStoichRowsChanged) {
    function init() {
        $scope.model = {
            reactants: reactants,
            selectedReactants: [],
            isSearchResultFound: false,
            databases: SearchService.getCatalogues()
        };

        $scope.tabs = _.map($scope.model.reactants, function(reactant) {
            return {formula: reactant.formula, searchResult: [], selectedReactant: null};
        });
    }

    $scope.addToStoichTable = function() {
        onStoichRowsChanged($scope.model.selectedReactants);
    };

    $scope.updateStoicAndExit = function() {
        var result = angular.copy($scope.model.reactants);
        _.each($scope.model.selectedReactants, function(knownReactant) {
            _.extend(_.findWhere(result, {formula: knownReactant.formula}), knownReactant);
        });
        onStoichRowsChanged(result);
        $uibModalInstance.close({});
    };

    $scope.search = function() {
        $scope.loading = true;
        _.each($scope.tabs, function(tab) {
            getSearchResult(tab.formula, function(searchResult) {
                $scope.loading = false;
                tab.searchResult = responseCallback(searchResult);
            });
        });
        $scope.isSearchCompleted = true;
    };

    $scope.cancel = function() {
        $uibModalInstance.close({});
    };

    function prepareDatabases() {
        return _.pluck(_.where($scope.model.databases, {isChecked: true}), 'value');
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
            structure: {formula: formula, searchMode: 'molformula'}
        };
        SearchService.search(searchRequest, callback);
    }

    init();
}
