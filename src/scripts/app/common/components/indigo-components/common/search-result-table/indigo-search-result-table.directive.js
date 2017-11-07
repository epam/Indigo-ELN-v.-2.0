var template = require('./search-result-table.html');

function indigoSearchResultTable() {
    return {
        restrict: 'E',
        template: template,
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

IndigoSearchResultTableController.$inject = ['userReagents', 'appValues', 'calculationService'];

function IndigoSearchResultTableController(userReagents, appValues, calculationService) {
    var vm = this;

    var itemBeforeEdit;

    $onInit();

    function $onInit() {
        vm.rxnValues = appValues.getRxnValues();
        vm.saltCodeValues = appValues.getSaltCodeValues();

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
        userReagents.save(vm.indigoTableContent);
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
        calculationService.recalculateSalt(reagent);
    }
}

module.exports = indigoSearchResultTable;

