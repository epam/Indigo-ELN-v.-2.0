require('./indigo-search-result-table.less');
var template = require('./search-result-table.html');

function indigoSearchResultTable() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            indigoTableContent: '=',
            indigoTableFilter: '=',
            indigoEditableInfo: '=',
            isMultiple: '=',
            indigoTab: '=',
            onChangeSelectedItems: '&',
            onSelected: '&'
        },
        controller: IndigoSearchResultTableController,
        controllerAs: 'vm',
        bindToController: true
    };
}

IndigoSearchResultTableController.$inject = ['userReagentsService', 'appValuesService', 'calculationService'];

function IndigoSearchResultTableController(userReagentsService, appValuesService, calculationService) {
    var vm = this;

    var itemBeforeEdit;

    $onInit();

    function $onInit() {
        vm.rxnValues = appValuesService.getRxnValues();
        vm.saltCodeValues = appValuesService.getSaltCodeValues();
        vm.selectedReactant = _.find(vm.indigoTableContent, '$$isSelected');

        vm.finishEdit = finishEdit;
        vm.cancelEdit = cancelEdit;
        vm.onSelectItems = onSelectItems;
        vm.selectSingleItem = selectSingleItem;
        vm.recalculateSalt = recalculateSalt;
        vm.editInfo = editInfo;
    }

    function editInfo(item) {
        itemBeforeEdit = angular.copy(item);
        vm.isEditMode = true;
    }

    function finishEdit() {
        vm.isEditMode = false;
        userReagentsService.save(vm.indigoTableContent);
    }

    function cancelEdit(index) {
        vm.indigoTableContent[index] = itemBeforeEdit;
        vm.isEditMode = false;
    }

    function onSelectItems(items) {
        vm.onChangeSelectedItems({items: _.filter(items, {$$isSelected: true})});
    }

    function selectSingleItem(reactant) {
        // _.forEach(vm.indigoTableContent, function(item) {
        //     if (reactant !== item) {
        //         item.$$isSelected = false;
        //     }
        // });
        vm.selectedReactant = reactant;

        vm.onSelected({item: reactant || null});
    }

    function recalculateSalt(reagent) {
        calculationService.recalculateSalt(reagent);
    }
}

module.exports = indigoSearchResultTable;

