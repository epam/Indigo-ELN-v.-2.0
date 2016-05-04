angular.module('indigoeln')
    .directive('searchResultTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/common/search-result-table/search-result-table.html',
            scope: {
                myTableContent: '=',
                myEditableInfo: '=',
                mySingleItemPerTab: '=',
                myTab: '=',
                mySelectedItemsPerTab: '='
            },
            controller: function ($scope, $http, UserReagents) {
                $scope.editInfo = function (item) {
                    $scope.itemBeforeEdit = angular.copy(item);
                    $scope.isEditMode = true;
                };

                $scope.finishEdit = function () {
                    $scope.isEditMode = false;
                    UserReagents.save($scope.myTableContent);
                };

                $scope.cancelEdit = function (index) {
                    $scope.myTableContent[index] = $scope.itemBeforeEdit;
                    $scope.isEditMode = false;
                };

                $scope.selectSingleItemtPerTab = function (tab, reactant, tabIndex, reactantIndex, isDeselected) {
                    if (!isDeselected) {
                        // only one reactant can be selected from each tab
                        var reactantToReplaceIndex = _.findIndex($scope.mySelectedItemsPerTab, {molFormula: reactant.molFormula});
                        if (reactantToReplaceIndex > -1) {
                            _.each(tab.searchResult, function (item, index) {
                                if (index !== reactantIndex) {
                                    item.$$isSelected = false;
                                }
                            });
                            $scope.mySelectedItemsPerTab[reactantToReplaceIndex] = reactant;
                        } else {
                            $scope.mySelectedItemsPerTab.push(reactant);
                        }
                    } else {
                        $scope.mySelectedItemsPerTab = _.without($scope.mySelectedItemsPerTab, reactant);
                    }
                };

                $scope.saltCodeValues = [
                    {name: '00 - Parent Structure', value: '0'},
                    {name: '01 - HYDROCHLORIDE', value: '1'},
                    {name: '02 - SODIUM', value: '2'},
                    {name: '03 - HYDRATE', value: '3'},
                    {name: '04 - HYDROBROMIDE', value: '4'},
                    {name: '05 - HYDROIODIDE', value: '5'},
                    {name: '06 - POTASSIUM', value: '6'},
                    {name: '07 - CALCIUM', value: '7'},
                    {name: '08 - SULFATE', value: '8'},
                    {name: '09 - PHOSPHATE', value: '9'},
                    {name: '10 - CITRATE', value: '10'}
                ];

                $scope.recalculateSalt = function (reagent) {
                    var config = {
                        params: {
                            saltCode: reagent.saltCode ? reagent.saltCode.value : null,
                            saltEq: reagent.saltEq
                        }
                    };
                    $http.put('api/calculations/molecule/info', reagent.structure.molfile, config)
                        .then(function (result) {
                            reagent.molWeight = result.data.molecularWeight;
                        });
                };
            }
        };
    });

