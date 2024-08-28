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

EditPurityController.$inject = ['$uibModalInstance', 'data', 'dictionary', 'appUnits'];

function EditPurityController($uibModalInstance, data, dictionary, appUnits) {
    var vm = this;

    init();

    function init() {
        vm.purity = data || {};
        vm.purity.data = vm.purity.data || [];
        vm.dictionary = dictionary;
        vm.operatorSelect = appUnits.operatorSelect;

        vm.unknownPurity = 'Purity Unknown';

        vm.isInknownPurity = isInknownPurity;
        vm.save = save;
        vm.cancel = cancel;
    }

    function isInknownPurity() {
        return vm.purity.property === vm.unknownPurity;
    }

    function resultToString() {
        var purityStrings = _.map(vm.purity.data, function(purity) {
            if (purity.operator && purity.value && purity.comments) {
                return purity.determinedBy + ' ' + purity.operator.name + ' ' +
                    purity.value + '% ' + purity.comments;
            } else if (purity.operator && purity.value) {
                return purity.determinedBy + ' ' + purity.operator.name + ' ' +
                    purity.value + '%';
            }

            return '';
        });

        return _.compact(purityStrings).join(', ');
    }

    function save() {
        if (vm.isInknownPurity()) {
            vm.purity = {};
            vm.purity.asString = vm.unknownPurity;
        } else {
            vm.purity.asString = resultToString();
        }
        $uibModalInstance.close(vm.purity);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EditPurityController;
