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

EditExternalSupplierController.$inject = ['$uibModalInstance', 'data'];

function EditExternalSupplierController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.externalSupplier = data || {};

        vm.externalSupplierCodeAndNameSelect = [
            {
                name: 'SPP1 - Supplier 1'
            },
            {
                name: 'SPP2 - Supplier 2'
            },
            {
                name: 'SPP3 - Supplier 3'
            }];

        vm.resultToString = resultToString;
        vm.cancel = cancel;
        vm.save = save;
    }

    function resultToString() {
        var res = '';
        if (vm.externalSupplier.codeAndName) {
            res += '<' + vm.externalSupplier.codeAndName.name + '> ';
        }
        if (vm.externalSupplier.catalogRegistryNumber) {
            res += vm.externalSupplier.catalogRegistryNumber;
        }

        return res;
    }

    function save() {
        vm.externalSupplier.asString = resultToString();
        $uibModalInstance.close(vm.externalSupplier);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EditExternalSupplierController;
