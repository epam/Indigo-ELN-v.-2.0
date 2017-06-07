(function () {
    angular
        .module('indigoeln')
        .directive('indigoSearchResultTable', indigoSearchResultTable);

    function indigoSearchResultTable() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/common/search-result-table/search-result-table.html',
            scope: {
                indigoTableContent: '=',
                indigoTableFilter: '=',
                indigoEditableInfo: '=',
                indigoSingleItemPerTab: '=',
                indigoTab: '=',
                indigoSelectedItemsPerTab: '='
            },
            controller: controller
        };
    }

    /* @ngInject */
    function controller($scope, $http, UserReagents, AppValues, $sce, CalculationService) {
        $scope.rxnValues = AppValues.getRxnValues();
        $scope.saltCodeValues = AppValues.getSaltCodeValues();

        $scope.editInfo = function (item) {
            $scope.itemBeforeEdit = angular.copy(item);
            $scope.isEditMode = true;
        };

        $scope.finishEdit = function () {
            $scope.isEditMode = false;
            UserReagents.save($scope.indigoTableContent);
        };

        $scope.cancelEdit = function (index) {
            $scope.indigoTableContent[index] = $scope.itemBeforeEdit;
            $scope.isEditMode = false;
        };

        $scope.selectSingleItemtPerTab = function (tab, reactant, tabIndex, reactantIndex, isDeselected) {
            if (!isDeselected) {
                // only one reactant can be selected from each tab
                var reactantToReplaceIndex = _.findIndex($scope.indigoSelectedItemsPerTab, {formula: reactant.formula});
                if (reactantToReplaceIndex > -1) {
                    _.each(tab.searchResult, function (item, index) {
                        if (index !== reactantIndex) {
                            item.$$isSelected = false;
                        }
                    });
                    $scope.indigoSelectedItemsPerTab[reactantToReplaceIndex] = reactant;
                } else {
                    $scope.indigoSelectedItemsPerTab.push(reactant);
                }
            } else {
                $scope.indigoSelectedItemsPerTab = _.without($scope.indigoSelectedItemsPerTab, reactant);
            }
            $scope.$apply();
        };

        $scope.recalculateSalt = function (reagent) {
            CalculationService.recalculateSalt(reagent);
        };

        var unsubscribe = $scope.$watch('indigoTableContent', function (newVal) {
            _.each(newVal, function (item) {
                item.$$popoverTemplate = $sce.trustAsHtml('<div><img class="img-fill" style="padding:10px;" ' +
                    'src="data:image/svg+xml;base64,' + item.structure.image + '" alt="Image is unavailable."></div>');
            });
        });
        $scope.$on('$destroy', function () {
            unsubscribe();
        });
    }
})();