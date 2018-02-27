/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

