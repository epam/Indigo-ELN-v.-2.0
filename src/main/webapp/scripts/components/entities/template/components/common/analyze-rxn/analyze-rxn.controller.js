angular.module('indigoeln').controller('AnalyzeRxnController',
    function ($scope, $rootScope, $uibModalInstance, $timeout, $http, reactants) {
        $scope.model = {};
        //$scope.model.reactants = ['C6 H6', 'C9 H12', 'C2 H6 O', 'C3 H5 N3 O9'];
        $scope.model.reactants = reactants;
        $scope.model.selectedReactants = [];
        $scope.isSearchResultFound = false;

        $scope.model.databases = [
            { key: 1, value: 'Indigo ELN', isChecked: true },
            { key: 2, value: 'Custom Catalog 1' },
            { key: 3, value: 'Custom Catalog 2' }
        ];

        var prepareDatabases = function() {
            return _.pluck(_.where($scope.model.databases, {isChecked: true}), 'value');
        };

        function getSearchResult(formula) {
            return $timeout(function() {
                return [
                    {
                        compoundId: 'STR-00000000',
                        casNumber: '123123123',
                        nbkBatch: '000-000',
                        chemicalName: 'benzene',
                        molWeight: '100',
                        weight: '200',
                        volume: '300',
                        mol: '50',
                        limiting: 'limiting',
                        rxnRole: 'rxn role',
                        molarity: 'molarity',
                        purity: 'pure',
                        molFormula: formula,
                        saltCode: 'salt code',
                        saltEq: 'salt eq',
                        loadFactor: '70%',
                        hazardComments: 'hazard comments',
                        comments: 'some comments',
                        database: 'IndigoELN',
                        structure: '#image',
                        $$isCollapsed: true,
                        $$isSelected: false
                    },
                    {
                        compoundId: 'STR-00111111',
                        casNumber: '121212121',
                        nbkBatch: '111-001',
                        chemicalName: 'benzene',
                        molWeight: '100',
                        weight: '200',
                        volume: '300',
                        mol: '50',
                        limiting: 'limiting',
                        rxnRole: 'rxn role',
                        molarity: 'molarity',
                        purity: 'very pure',
                        molFormula: formula,
                        saltCode: 'salt code',
                        saltEq: 'salt eq',
                        loadFactor: '70%',
                        hazardComments: 'hazard comments',
                        comments: 'some comments',
                        database: 'IndigoELN',
                        structure: '#image',
                        $$isCollapsed: true,
                        $$isSelected: false
                    }
                ];
            });
            /*var searchCriteria = {
             databases: prepareDatabases(),
             structure: { formula: formula }
             };
             $http({
             url: 'api/search/batch',
             method: 'POST',
             data: searchCriteria
             }).success(function (result) {
             return _.map(result, function (item) {
             var batchDetails = _.extend({}, item.details);
             batchDetails.nbkBatch = item.notebookBatchNumber;
             batchDetails.$$isCollapsed = true;
             batchDetails.$$isSelected = false;
             batchDetails.database = $scope.model.databases.join(', ');
             batchDetails.molWeight = item.details.molWt;
             return batchDetails;
             });
             });*/
        }

        $scope.tabs = _.map($scope.model.reactants, function(reactant) {
            return { formula: reactant, searchResult: [], selectedReactant: null };
        });

        $scope.addToStoichTable = function () {
            $rootScope.$broadcast('new-stoich-rows', $scope.model.selectedReactants);
        };

        $scope.search = function () {
            _.each($scope.tabs, function(tab) {
                getSearchResult(tab.formula).then(function(searchResult){
                    tab.searchResult = searchResult;
                });
            });
            $scope.isSearchCompleted = true;
        };

        $scope.cancel = function () {
            $uibModalInstance.close({});
        };
    });
