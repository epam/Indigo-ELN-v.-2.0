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

EditMeltingPointController.$inject = ['$uibModalInstance', 'data'];

function EditMeltingPointController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.meltingPoint = data || {};

        vm.save = save;
        vm.cancel = cancel;
    }

    function resultToString() {
        if (vm.meltingPoint.lower && vm.meltingPoint.upper) {
            return vm.meltingPoint.lower + ' ~ ' + vm.meltingPoint.upper + '\xB0C' + addCommentIfExist();
        }

        return null;
    }

    function addCommentIfExist() {
        return vm.meltingPoint.comments ? ', ' + vm.meltingPoint.comments : '';
    }

    function save() {
        vm.meltingPoint.asString = resultToString();
        $uibModalInstance.close(vm.meltingPoint);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EditMeltingPointController;
