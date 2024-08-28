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
var template = require('./add-new-batch.html');

function addNewBatch() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'addNewBatch'],
        scope: {
            batchOperation: '=',
            isReadonly: '=',
            onAddedBatch: '&',
            onSelectBatch: '&'
        },
        template: template,
        controller: AddNewBatchController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            controllers[1].indigoComponents = controllers[0];
            $element.addClass('component-button');
        }
    };
}

AddNewBatchController.$inject = ['productBatchSummaryOperations'];

function AddNewBatchController(productBatchSummaryOperations) {
    var vm = this;

    init();

    function init() {
        vm.addNewBatch = addNewBatch;
    }

    function addNewBatch() {
        vm.batchOperation = productBatchSummaryOperations
            .addNewBatch(vm.indigoComponents.experiment)
            .then(successAddedBatch);
    }

    function successAddedBatch(batch) {
        vm.onAddedBatch({batch: batch});
        vm.onSelectBatch({batch: batch});
    }
}

module.exports = addNewBatch;
