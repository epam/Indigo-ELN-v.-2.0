(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoSearchResultTable', indigoSearchResultTable);

    function indigoSearchResultTable() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/search-result-table/search-result-table.html',
            scope: {
                indigoTableContent: '=',
                indigoTableFilter: '=',
                indigoEditableInfo: '=',
                indigoSingleItemPerTab: '=',
                indigoTab: '=',
                indigoSelectedItemsPerTab: '=',
                onChangeSelectedItems: '&'
            },
            controller: IndigoSearchResultTableController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    IndigoSearchResultTableController.$inject = ['UserReagents', 'AppValues', 'CalculationService'];

    function IndigoSearchResultTableController(UserReagents, AppValues, CalculationService) {
        var vm = this;

        var itemBeforeEdit;

        $onInit();

        function $onInit() {
            vm.rxnValues = AppValues.getRxnValues();
            vm.saltCodeValues = AppValues.getSaltCodeValues();

            vm.finishEdit = finishEdit;
            vm.cancelEdit = cancelEdit;
            vm.onSelectItems = onSelectItems;
            vm.selectSingleItemtPerTab = selectSingleItemtPerTab;
            vm.recalculateSalt = recalculateSalt;
            vm.editInfo = editInfo;
        }

        function editInfo(item) {
            itemBeforeEdit = angular.copy(item);
            vm.isEditMode = true;
        }

        function finishEdit() {
            vm.isEditMode = false;
            UserReagents.save(vm.indigoTableContent);
        }

        function cancelEdit(index) {
            vm.indigoTableContent[index] = itemBeforeEdit;
            vm.isEditMode = false;
        }

        function onSelectItems(items) {
            vm.onChangeSelectedItems({items: _.filter(items, {$$isSelected: true})});
        }

        function selectSingleItemtPerTab(tab, reactant, reactantIndex, isDeselected) {
            if (!isDeselected) {
                // only one reactant can be selected from each tab
                var reactantToReplaceIndex = _.findIndex(vm.indigoSelectedItemsPerTab, {
                    formula: reactant.formula
                });
                if (reactantToReplaceIndex > -1) {
                    _.each(tab.searchResult, function(item, index) {
                        if (index !== reactantIndex) {
                            item.$$isSelected = false;
                        }
                    });
                    vm.indigoSelectedItemsPerTab[reactantToReplaceIndex] = reactant;
                } else {
                    vm.indigoSelectedItemsPerTab.push(reactant);
                }
            } else {
                vm.indigoSelectedItemsPerTab = _.without(vm.indigoSelectedItemsPerTab, reactant);
            }
        }

        function recalculateSalt(reagent) {
            CalculationService.recalculateSalt(reagent);
        }
    }
})();
