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

StructureValidationController.$inject = ['$uibModalInstance', 'batches', 'searchQuery', 'appValuesService'];

function StructureValidationController($uibModalInstance, batches, searchQuery, appValuesService) {
    var vm = this;

    init();

    function init() {
        vm.batches = batches;
        vm.searchQuery = searchQuery;
        vm.selectedBatch = null;
        vm.defaultSaltCodeName = appValuesService.getDefaultSaltCode().name;

        vm.save = save;
        vm.cancel = cancel;
        vm.selectBatch = selectBatch;
    }

    function selectBatch(batch) {
        if (vm.selectedBatch) {
            vm.selectedBatch.$$isSelected = false;
        }
        batch.$$isSelected = true;
        vm.selectedBatch = batch;
    }

    function save() {
        $uibModalInstance.close(vm.selectedBatch);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = StructureValidationController;
