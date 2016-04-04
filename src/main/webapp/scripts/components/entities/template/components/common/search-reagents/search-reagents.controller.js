'use strict';

angular.module('indigoeln').controller('SearchReagentsController',
    function ($scope, $rootScope, $uibModalInstance, $timeout, Alert, activeTab) {
        $scope.model = {};
        $scope.isSearchResultFound = false;
        $scope.model.restrictions = {
            searchQuery: '',
            advancedSearch: {
                nbkBatch: {name: 'NBK batch #', searchCondition: {name: 'contains'}},
                molFormula: {name: 'Molecular Formula', searchCondition: {name: 'contains'}},
                molWeight: {name: 'Molecular Weight', searchCondition: {name: '>'}},
                chemicalName: {name: 'Chemical Name', searchCondition: {name: 'contains'}},
                externalNumber: {name: 'External #', searchCondition: {name: 'contains'}},
                compoundState: {name: 'Compound State'},
                comments: {name: 'Batch Comment', searchCondition: {name: 'contains'}},
                hazardComments: {name: 'Batch Hazard Comment', searchCondition: {name: 'contains'}},
                casNumber: {name: 'CAS Number', searchCondition: {name: 'contains'}}
            },
            structure: {
                name: 'Reaction Scheme',
                similarityCriteria: {name: 'none'},
                similarityValue: null,
                scheme: null,
                image: null,
                molFile: null
            }
        };

        $scope.addToStoichTable = function (list) {
            var selected = _.where(list, {isSelected: true});
            $rootScope.$broadcast('new-stoich-rows', selected);
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

        $scope.myReagentList = [];

        $scope.addToMyReagentList = function () {
            var selected = _.where($scope.searchResults, {isSelected: true});
            var selectedPure, myReagentListPure;
            var count = 0;
            selectedPure = _.map(selected, function(item) {
                return _.omit(item, 'isSelected', 'isCollapsed');
            });
            myReagentListPure = _.map($scope.myReagentList, function(item) {
                return _.omit(item, 'isSelected', 'isCollapsed');
            });
            _.each(selectedPure, function(selectedItem) {
                var isUnique = _.every(myReagentListPure, function(myListItem) {
                    return !angular.equals(selectedItem, myListItem);
                });
                if (isUnique) {
                    selectedItem.isSelected = false;
                    selectedItem.isCollapsed = true;
                    $scope.myReagentList.push(selectedItem);
                    count = count + 1;
                }
            });
            if (count === 1) {
                Alert.info(count + ' reagent successfully added to My Reagent List');
            } else if(count > 0) {
                Alert.info(count + ' reagents successfully added to My Reagent List');
            } else {
                Alert.warning('My Reagent List already contains selected reagents');
            }
        };

        $scope.removeFromMyReagentList = function () {
            var selected = _.where($scope.myReagentList, {isSelected: true});
            _.each(selected, function(item) {
                $scope.myReagentList = _.without($scope.myReagentList, item);
            });
        };

        $scope.isEditMode = false;

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
                        molFormula: 'C6 H6',
                        saltCode: 'salt code',
                        saltEq: 'salt eq',
                        loadFactor: '70%',
                        hazardComments: 'hazard comments',
                        comments: 'some comments',
                        database: 'IndigoELN',
                        structure: '#image',
                        isCollapsed: true,
                        isSelected: false
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
                        molFormula: 'C6 H6',
                        saltCode: 'salt code',
                        saltEq: 'salt eq',
                        loadFactor: '70%',
                        hazardComments: 'hazard comments',
                        comments: 'some comments',
                        database: 'IndigoELN',
                        structure: '#image',
                        isCollapsed: true,
                        isSelected: false
                    },
                    {
                        compoundId: 'STR-002222222',
                        casNumber: '232323232',
                        nbkBatch: '222-002',
                        chemicalName: 'benzene',
                        molWeight: '100',
                        weight: '200',
                        volume: '300',
                        mol: '50',
                        limiting: 'limiting',
                        rxnRole: 'rxn role',
                        molarity: 'molarity',
                        purity: 'so pure',
                        molFormula: 'C6 H6 ',
                        saltCode: 'salt code',
                        saltEq: 'salt eq',
                        loadFactor: '70%',
                        hazardComments: 'hazard comments',
                        comments: 'some comments',
                        database: 'IndigoELN',
                        structure: '#image',
                        isCollapsed: true,
                        isSelected: false
                    }
                ];
            });
        };

        $scope.cancel = function () {
            $uibModalInstance.close({});
        };
    });
