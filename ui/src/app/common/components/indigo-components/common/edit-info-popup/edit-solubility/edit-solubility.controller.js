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

EditSolubilityController.$inject = ['$uibModalInstance', 'solubility', 'appUnits'];

function EditSolubilityController($uibModalInstance, solubility, appUnits) {
    var vm = this;

    init();

    function init() {
        vm.solubility = getSolubility();
        vm.solubilityTypeSelect = [
            {name: 'Quantitative'},
            {name: 'Qualitative'}];

        vm.qualitativeSolubilitySelect = [
            {name: 'Soluble'},
            {name: 'Unsoluble'},
            {name: 'Precipitate'}];

        vm.unitSelect = [
            {name: 'g/ml'}];

        vm.operatorSelect = appUnits.operatorSelect;

        vm.save = save;
        vm.cancel = cancel;
        vm.addSolvent = addSolvent;
        vm.remove = remove;
        vm.removeAll = removeAll;
    }

    function getSolubility() {
        var newSolubility = solubility || {};
        newSolubility.data = newSolubility.data || [];

        return newSolubility;
    }

    function addSolvent() {
        vm.solubility.data.push({
            solventName: {},
            type: {},
            value: {},
            comment: ''
        });
    }

    function remove(solvent) {
        vm.solubility.data = _.without(vm.solubility.data, solvent);
    }

    function removeAll() {
        vm.solubility.data = [];
    }

    function resultToString() {
        var solubilityStrings = _.map(vm.solubility.data, function(sb) {
            var solvent = sb.solventName ? sb.solventName.name : null;
            var type = sb.type ? sb.type.name : null;
            var val = sb.value ? sb.value : null;
            var result = '';
            if (type === 'Quantitative' && val) {
                result = val.operator.name + ' ' + val.value + ' ' + val.unit.name;
            } else if (val.value) {
                result = val.value.name;
            }
            if (result && solvent) {
                result += ' in ' + solvent;
            }

            return result;
        });

        return _.compact(solubilityStrings).join(', ');
    }

    function save() {
        vm.solubility.asString = resultToString();
        $uibModalInstance.close(vm.solubility);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EditSolubilityController;
