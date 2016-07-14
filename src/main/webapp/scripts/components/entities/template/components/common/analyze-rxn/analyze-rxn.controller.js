angular.module('indigoeln').controller('AnalyzeRxnController',
    function ($scope, $rootScope, $uibModalInstance, $timeout, $http, reactants, $q, SearchService, AppValues) {
        $scope.model = {};
        $scope.model.reactants = reactants;
        $scope.model.selectedReactants = [];
        $scope.isSearchResultFound = false;

        $scope.model.databases = SearchService.getCatalogues();

        $scope.tabs = _.map($scope.model.reactants, function (reactant) {
            return {formula: reactant, searchResult: [], selectedReactant: null};
        });

        $scope.addToStoichTable = function () {
            $rootScope.$broadcast('stoich-rows-changed', $scope.model.selectedReactants);
        };

        var prepareDatabases = function () {
            return _.pluck(_.where($scope.model.databases, {isChecked: true}), 'value');
        };

        function responseCallback(result) {
            var databases = prepareDatabases();
            return _.map(result, function (item) {
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

        $scope.search = function () {
            _.each($scope.tabs, function (tab) {
                getSearchResult(tab.formula, function (searchResult) {
                    tab.searchResult = responseCallback(searchResult);
                });
            });
            $scope.isSearchCompleted = true;
        };

        $scope.cancel = function () {
            $uibModalInstance.close({});
        };
    });
