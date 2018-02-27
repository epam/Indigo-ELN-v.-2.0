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

/* @ngInject */
function NotebookSelectParentController($uibModalInstance, parents, principalService, simpleLocalCache) {
    var vm = this;
    var lastSelectedProjectIdKey = '.lastSelectedProjectId';

    vm.parents = parents;
    vm.selectedParent = '';

    vm.ok = okPressed;
    vm.cancel = cancelPressed;
    vm.onSelect = onSelect;

    init();

    function okPressed() {
        $uibModalInstance.close(vm.selectedParent.id);
    }

    function cancelPressed() {
        $uibModalInstance.dismiss();
    }

    function onSelect() {
        simpleLocalCache.putByKey(getKey(lastSelectedProjectIdKey), vm.selectedParent.id);
    }

    function getKey(suffix) {
        return principalService.getIdentity().id + suffix;
    }

    function init() {
        var lastSelectedProjectId = simpleLocalCache.getByKey(getKey(lastSelectedProjectIdKey));

        if (lastSelectedProjectId) {
            vm.selectedParent = _.find(parents, {id: lastSelectedProjectId});
        }
    }
}

module.exports = NotebookSelectParentController;
