'use strict';

angular.module('indigoeln').controller('AnalyzeRxnController',
    function ($scope, $rootScope, $uibModalInstance, $timeout, $http, Alert, reactants) {
        $scope.model = {};
        //$scope.model.reactants = ['C6 H6', 'C9 H12', 'C2 H6 O', 'C3 H5 N3 O9'];
        $scope.model.reactants = reactants;
        $scope.model.selectedReactants = [];
        $scope.isEditMode = false;
        $scope.isSearchResultFound = false;
        $scope.allItemsSelected = false;

        $scope.model.databases = [
            { key: 1, value: 'Indigo ELN', isChecked: true },
            { key: 2, value: 'Custom Catalog 1' },
            { key: 3, value: 'Custom Catalog 2' }
        ];

        $scope.model.selectDatabase = function () {
            for (var i = 0; i < $scope.model.databases.length; i++) {
                if (!$scope.model.databases[i].isChecked) {
                    $scope.allItemsSelected = false;
                    return;
                }
            }
            $scope.allItemsSelected = true;
        };

        $scope.selectAll = function () {
            for (var i = 0; i < $scope.model.databases.length; i++) {
                $scope.model.databases[i].isChecked = $scope.allItemsSelected;
            }
        };

        $scope.editInfo = function (item) {
            $scope.itemBeforeEdit = angular.copy(item);
            $scope.isEditMode = true;
        };

        $scope.finishEdit = function () {
            $scope.isEditMode = false;
        };

        $scope.cancelEdit = function (index) {
            $scope.myReagentList[index] = $scope.itemBeforeEdit;
            $scope.isEditMode = false;
        };

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
            //var searchCriteria = {
            //    databases: prepareDatabases(),
            //    structure: { formula: formula }
            //};
            //$http({
            //    url: 'api/search/batch',
            //    method: 'POST',
            //    data: searchCriteria
            //}).success(function (result) {
            //    return _.map(result, function (item) {
            //        var batchDetails = _.extend({}, item.details);
            //        batchDetails.nbkBatch = item.notebookBatchNumber;
            //        batchDetails.$$isCollapsed = true;
            //        batchDetails.$$isSelected = false;
            //        batchDetails.database = $scope.model.databases.join(', ');
            //        batchDetails.molWeight = item.details.molWgt;
            //        return batchDetails;
            //    });
            //});
        }
        
        $scope.model.selectDatabase = function () {
            for (var i = 0; i < $scope.model.databases.length; i++) {
                if (!$scope.model.databases[i].isChecked) {
                    $scope.allItemsSelected = false;
                    return;
                }
            }
            $scope.allItemsSelected = true;
        };

        $scope.tabs = _.map($scope.model.reactants, function(reactant) {
            return { formula: reactant, searchResult: [], selectedReactant: null };
        });

        $scope.selectReactant = function (tab, reactant, tabIndex, reactantIndex, $$isSelected) {
            if ($$isSelected) {
                // only one reactant can be selected from each tab
                var reactantToReplaceIndex = _.findIndex($scope.model.selectedReactants, {molFormula: reactant.molFormula});
                if (reactantToReplaceIndex > -1) {
                    _.each(tab.searchResult, function(item, index) {
                        if (index !== reactantIndex) {
                            item.$$isSelected = false;
                        }
                    });
                    $scope.model.selectedReactants[reactantToReplaceIndex] = reactant;
                } else {
                    $scope.model.selectedReactants.push(reactant);
                }
            } else {
                $scope.model.selectedReactants = _.without($scope.model.selectedReactants, reactant);
            }
        };

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
