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
var template = require('./duplicate-batches.html');

function duplicateBatches() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'duplicateBatches'],
        scope: {
            isReadonly: '=',
            batches: '='
        },
        template: template,
        controller: DuplicateBatchesController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };
}

DuplicateBatchesController.$inject = ['productBatchSummaryOperations', 'batchHelper'];

function DuplicateBatchesController(productBatchSummaryOperations, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.duplicateBatches = duplicateBatches;
    }

    function duplicateBatches() {
        vm.indigoComponents.batchOperation = productBatchSummaryOperations
            .duplicateBatches(batchHelper.getCheckedBatches(vm.batches), false, vm.indigoComponents.experiment)
            .then(successAddedBatches);
    }

    function successAddedBatches(batches) {
        if (batches.length) {
            _.forEach(batches, function(batch) {
                vm.indigoComponents.onAddedBatch(batch);
            });
            var checkedBatches = batchHelper.getCheckedBatches(vm.batches);
            _.forEach(checkedBatches, function(batch) {
                batch.$$select = false;
            });
            vm.indigoComponents.onSelectBatch(_.last(batches));
        }
    }
}

module.exports = duplicateBatches;
