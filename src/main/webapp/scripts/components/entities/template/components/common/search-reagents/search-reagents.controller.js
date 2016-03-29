'use strict';

angular.module('indigoeln').controller('SearchReagentsController',
    function ($scope, $uibModalInstance, $timeout, activeTab) {
        $scope.model = {};
        $scope.isSearchResultFound = false;
        $scope.model.restrictions = {
            searchQuery: '',
            advancedSearch: {
                batchNumber: {name: 'Batch Number', searchCondition: {name: 'contains'}},
                molecularFormula: {name: 'Molecular Formula', searchCondition: {name: 'contains'}},
                molecularWeight: {name: 'Molecular Weight', searchCondition: {name: '>'}},
                chemicalName: {name: 'Chemical Name', searchCondition: {name: 'contains'}},
                externalNumber: {name: 'External #', searchCondition: {name: 'contains'}},
                compoundState: {name: 'Compound State'},
                batchComment: {name: 'Batch Comment', searchCondition: {name: 'contains'}},
                batchHazardComment: {name: 'Batch Hazard Comment', searchCondition: {name: 'contains'}},
                casNumber: {name: 'CAS Number', searchCondition: {name: 'contains'}}
            },
            structure: {name: 'Reaction Scheme', similarityCriteria: {name: 'none'}, scheme: null}
        };

        $scope.searchConditionText = [{name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}];
        $scope.searchConditionChemicalName = [{name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}];
        $scope.searchConditionNumber = [{name: '>'}, {name: '<'}, {name: '='}];
        $scope.searchConditionSimilarity = [{name:'none'},{name:'equal'},{name:'substructure'},{name:'similarity'}];

        $scope.isActiveTab0 = activeTab === 0;
        $scope.isActiveTab1 = activeTab === 1;
        $scope.model.allItemsSelected = false;
        $scope.model.databases = [
            { key: 1, value: 'Indigo ELN', isChecked: true },
            { key: 2, value: 'Custom Catalog 1' },
            { key: 3, value: 'Custom Catalog 2' }
        ];
        $scope.model.selectDatabase = function () {
            for (var i = 0; i < $scope.model.databases.length; i++) {
                if (!$scope.model.databases[i].isChecked) {
                    $scope.model.allItemsSelected = false;
                    return;
                }
            }
            $scope.model.allItemsSelected = true;
        };
        $scope.selectAll = function () {
            for (var i = 0; i < $scope.model.databases.length; i++) {
                $scope.model.databases[i].isChecked = $scope.model.allItemsSelected;
            }
        };

        $scope.isAdvancedSearchFilled = function () {
            return !!_.compact(_.map($scope.model.restrictions.advancedSearch, function (restriction) {
                return restriction.value;
            })).length;
        };

        $scope.search = function () {
            $scope.model.databases = _.pluck(_.where($scope.model.databases, {isChecked: true}), 'value');

            $timeout(function () {
                $scope.model.restrictions.advancedSummary = [];
                _.each($scope.model.restrictions.advancedSearch, function (restriction) {
                    if (restriction.value) {
                        $scope.model.restrictions.advancedSummary.push(restriction);
                    }
                });
                $scope.isSearchResultFound = true;
                $scope.searchResults = [
                    {
                        molecularWeight: '88',
                        molecularFormula: 'C6 H6',
                        reagentName: 'STR-00111111',
                        database: 'IndigoELN',
                        structure: '#image',
                        isCollapsed: true,
                        isSelected: false
                    },
                    {
                        molecularWeight: '48',
                        molecularFormula: 'C6 H6',
                        reagentName: 'STR-00222222',
                        database: 'IndigoELN',
                        structure: '#image',
                        isCollapsed: true,
                        isSelected: false
                    },
                    {
                        molecularWeight: '45',
                        molecularFormula: 'C6 H6',
                        reagentName: 'STR-00333333',
                        database: 'IndigoELN',
                        structure: '#image',
                        isCollapsed: true,
                        isSelected: false
                    }
                ];
            });
        };
        $scope.myReagentList = []; // todo take my reagent list from server
        $scope.addToMyReagentList = function () {
            var selected = angular.copy(_.where($scope.searchResults, {isSelected: true}));
            var selectedReagents = _.map(selected, function(reagent) {
                var reagentCopy = angular.copy(reagent);
                reagentCopy.isSelected = false;
                reagentCopy.isCollapsed = true;
                return reagentCopy;
            });
            $scope.myReagentList = _.union($scope.myReagentList, selectedReagents);
        };
        $scope.removeFromMyReagentList = function () {
            var selected = _.where($scope.myReagentList, {isSelected: true});
            $scope.myReagentList = _.without($scope.myReagentList, selected);
        };
        $scope.addToExperiment = function () {

        };
        $scope.addToTable = function () {

        };
        $scope.saveOriginalBeforeEdit = function () {

        };
        $scope.saveEditedItem = function () {

        };
        $scope.cancelEdit = function () {

        };

        $scope.cancel = function () {
            $uibModalInstance.close();
        };
    });
