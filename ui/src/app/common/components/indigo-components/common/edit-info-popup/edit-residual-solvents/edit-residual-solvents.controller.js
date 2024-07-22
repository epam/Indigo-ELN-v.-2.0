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

EditResidualSolventsController.$inject = ['$uibModalInstance', 'solvents'];

function EditResidualSolventsController($uibModalInstance, solvents) {
    var vm = this;

    init();

    function init() {
        vm.solvents = solvents;
        vm.save = save;
        vm.cancel = cancel;
        vm.addSolvent = addSolvent;
        vm.remove = remove;
        vm.removeAll = removeAll;
    }

    function addSolvent() {
        vm.solvents.push({
            name: '', eq: '', comment: ''
        });
    }

    function remove(solvent) {
        vm.solvents = _.without(vm.solvents, solvent);
    }

    function removeAll() {
        vm.solvents = [];
    }

    function resultToString() {
        var solventStrings = _.map(vm.solvents, function(solvent) {
            if (solvent.name && solvent.eq) {
                return solvent.eq + ' mols of ' + solvent.name.name;
            }

            return '';
        });

        return _.compact(solventStrings).join(', ');
    }

    function save() {
        $uibModalInstance.close({
            data: vm.solvents,
            asString: resultToString()
        });
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EditResidualSolventsController;
