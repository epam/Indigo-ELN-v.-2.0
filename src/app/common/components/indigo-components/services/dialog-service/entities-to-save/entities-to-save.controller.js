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
function EntitiesToSaveController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.entities = _.map(data, function(entity) {
            entity.$$saveEntity = true;

            return entity;
        });

        vm.save = save;
        vm.cancel = cancel;
        vm.isSelected = isSelected;
        vm.discardAll = discardAll;
    }

    function isSelected() {
        return _.some(vm.entities, function(entity) {
            return entity.$$saveEntity;
        });
    }

    function discardAll() {
        _.each(vm.entities, function(entity) {
            entity.$$saveEntity = false;
        });
        $uibModalInstance.close([]);
    }

    function save() {
        var entitiesToSave = _.filter(vm.entities, function(entity) {
            return entity.$$saveEntity;
        });
        $uibModalInstance.close(entitiesToSave);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EntitiesToSaveController;
