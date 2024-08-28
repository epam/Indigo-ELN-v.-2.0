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

/* eslint no-shadow: "off"*/
var template = require('./delete-batches.html');

function deleteBatches() {
    return {
        restrict: 'E',
        scope: {
            isReadonly: '=',
            batches: '=?',
            deleteBatches: '=?',
            deleteBatch: '=?',
            onRemoveBatches: '&'
        },
        template: template,
        controller: DeleteBatchesController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element) {
            $element.addClass('component-button');
        }
    };

    function DeleteBatchesController() {
        var vm = this;

        init();

        function init() {
            vm.deleteBatches = deleteBatches;
        }

        function deleteBatches() {
            vm.onRemoveBatches({batches: getBatches()});
        }

        function getBatches() {
            if (_.isArray(vm.deleteBatches)) {
                return vm.deleteBatches;
            }
            if (_.isArray(vm.batches)) {
                return _.filter(vm.batches, {$$select: true});
            }

            return vm.deleteBatch ? [vm.deleteBatch] : null;
        }
    }
}

module.exports = deleteBatches;
