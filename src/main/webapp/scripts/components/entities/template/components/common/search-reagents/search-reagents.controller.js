angular.module('indigoeln').controller('SearchReagentsController',
    function ($scope, $rootScope, $uibModalInstance, $http, Alert, activeTab, UserReagents) {
        $scope.model = {};
        $scope.isSearchResultFound = false;
        $scope.model.restrictions = {
            searchQuery: '',
            advancedSearch: {
                nbkBatch: {name: 'NBK batch #', field: 'nbkBatch', condition: {name: 'contains'}},
                molFormula: {name: 'Molecular Formula', field: 'molFormula', condition: {name: 'contains'}},
                molWeight: {name: 'Molecular Weight', field: 'molWeight', condition: {name: '>'}},
                chemicalName: {name: 'Chemical Name', field: 'chemicalName', condition: {name: 'contains'}},
                externalNumber: {name: 'External #', field: 'externalNumber', condition: {name: 'contains'}},
                compoundState: {name: 'Compound State', field: 'compoundState'},
                comments: {name: 'Batch Comment', field: 'comments', condition: {name: 'contains'}},
                hazardComments: {name: 'Batch Hazard Comment', field: 'hazardComments', condition: {name: 'contains'}},
                casNumber: {name: 'CAS Number', field: 'casNumber', condition: {name: 'contains'}}
            },
            structure: {
                name: 'Reaction Scheme',
                similarityCriteria: {name: 'equal'},
                similarityValue: null,
                image: null
            }
        };

        $scope.addToStoichTable = function (list) {
            var selected = _.where(list, {$$isSelected: true});
            $rootScope.$broadcast('new-stoich-rows', selected);
        };

        $scope.conditionText = [{name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}];
        $scope.conditionChemicalName = [{name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}];
        $scope.conditionNumber = [{name: '>'}, {name: '<'}, {name: '='}];
        $scope.conditionSimilarity = [{name: 'equal'}, {name: 'substructure'}, {name: 'similarity'}];

        $scope.isActiveTab0 = activeTab === 0;
        $scope.isActiveTab1 = activeTab === 1;
        $scope.model.databases = [
            { key: 1, value: 'Indigo ELN', isChecked: true },
            { key: 2, value: 'Custom Catalog 1' },
            { key: 3, value: 'Custom Catalog 2' }
        ];

        UserReagents.get({}, function (reagents) {
            $scope.myReagentList = _.map(reagents, function (reagent) {
                reagent.$$isSelected = false;
                reagent.$$isCollapsed = true;
                return reagent;
            });
        });

        $scope.addToMyReagentList = function () {
            var selected = _.where($scope.searchResults, {$$isSelected: true});
            var count = 0;
            _.each(selected, function (selectedItem) {
                var isUnique = _.every($scope.myReagentList, function (myListItem) {
                    return !angular.equals(selectedItem, myListItem);
                });
                if (isUnique) {
                    selectedItem.$$isSelected = false;
                    selectedItem.$$isCollapsed = true;
                    $scope.myReagentList.push(selectedItem);
                    count = count + 1;
                }
            });
            if (count > 0) {
                UserReagents.save($scope.myReagentList, function () {
                    if (count === 1) {
                        Alert.info(count + ' reagent successfully added to My Reagent List');
                    } else if (count > 0) {
                        Alert.info(count + ' reagents successfully added to My Reagent List');
                    }
                });
            } else {
                Alert.warning('My Reagent List already contains selected reagents');
            }
        };

        $scope.removeFromMyReagentList = function () {
            var selected = _.where($scope.myReagentList, {$$isSelected: true});
            _.each(selected, function(item) {
                $scope.myReagentList = _.without($scope.myReagentList, item);
            });
            UserReagents.save($scope.myReagentList);
        };

        $scope.isAdvancedSearchFilled = function () {
            return !!_.compact(_.map($scope.model.restrictions.advancedSearch, function (restriction) {
                return restriction.value;
            })).length;
        };

        function prepareDatabases() {
            return _.pluck(_.where($scope.model.databases, {isChecked: true}), 'value');
        }

        function prepareAdvancedSearch() {
            var advancedSearch = $scope.model.restrictions.advancedSearch;
            var advancedSummary = $scope.model.restrictions.advancedSummary = [];
            _.each(advancedSearch, function (restriction) {
                if (restriction.value) {
                    var restrictionCopy = angular.copy(restriction);
                    restrictionCopy.condition = restrictionCopy.condition.name;
                    advancedSummary.push(restrictionCopy);
                }
            });
            return advancedSummary.length ? advancedSummary : null;
        }

        function prepareStructure() {
            if (!$scope.model.restrictions.structure.molfile) {
                return null;
            }
            var structure = $scope.model.restrictions.structure;
            var searchMode = $scope.model.restrictions.structure.similarityCriteria.name;
            if (searchMode === 'equal') {
                searchMode = 'exact';
            }
            structure.searchMode = searchMode;
            structure.similarity = $scope.model.restrictions.structure.similarityValue / 100;
            return structure;
        }

        function prepareSearchRequest() {
            var searchRequest = {};
            if ($scope.model.restrictions.searchQuery) {
                searchRequest.searchQuery = $scope.model.restrictions.searchQuery;
            }
            if (prepareAdvancedSearch()) {
                searchRequest.advancedSearch = prepareAdvancedSearch();
            }
            if (prepareStructure()) {
                searchRequest.structure = prepareStructure();
            }
            searchRequest.databases = $scope.databases = prepareDatabases();
            return searchRequest;
        }

        function responseCallback(result) {
            $scope.searchResults = _.map(result, function (item) {
                var batchDetails = _.extend({}, item.details);
                batchDetails.$$isCollapsed = true;
                batchDetails.$$isSelected = false;
                batchDetails.nbkBatch = item.notebookBatchNumber;
                batchDetails.database = $scope.databases.join(', ');
                return batchDetails;
            });
        }

        $scope.search = function () {
            var searchRequest = prepareSearchRequest();
            $http({
                url: 'api/search/batch',
                method: 'POST',
                data: searchRequest
            }).success(function (result) {
                responseCallback(result);
            });
            $scope.isSearchResultFound = true;
        };

        $scope.cancel = function () {
            $uibModalInstance.close({});
        };
    });
