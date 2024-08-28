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

var template = require('./prefer-compound-details.html');

function indigoPreferredCompoundDetails() {
    return {
        restrict: 'E',
        replace: true,
        template: template,
        controller: IndigoPreferredCompoundDetailsController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            componentData: '=',
            batches: '=',
            batchesTrigger: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            isReadonly: '=',
            batchOperation: '=',
            onSelectBatch: '&',
            onAddedBatch: '&',
            onRemoveBatches: '&',
            structureSize: '=',
            isHideColumnSettings: '=',
            isExistStoichTable: '=',
            onChanged: '&'
        }
    };
}

IndigoPreferredCompoundDetailsController.$inject = ['$scope', 'entitiesBrowserService',
    'appValuesService', 'batchHelper'];

function IndigoPreferredCompoundDetailsController($scope, entitiesBrowserService,
                                                  appValuesService, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.showSummary = false;
        vm.notebookId = entitiesBrowserService.getActiveTab().$$title;
        vm.saltCodeValues = appValuesService.getSaltCodeValues();
        vm.hasCheckedRows = batchHelper.hasCheckedRow;
        vm.selectBatch = selectBatch;
        vm.canEditSaltEq = batchHelper.canEditSaltEq;
        vm.canEditSaltCode = batchHelper.canEditSaltCode;
        vm.onBatchChanged = batchHelper.onBatchChanged;

        bindEvents();
    }

    function selectBatch(batch) {
        vm.onSelectBatch({batch: batch});
    }

    function checkEditDisabled() {
        return vm.isReadonly
            || !vm.selectedBatch
            || !vm.selectedBatch.nbkBatch
            || !!vm.selectedBatch.registrationStatus;
    }

    function bindEvents() {
        $scope.$watch(checkEditDisabled, function(newValue) {
            vm.isEditDisabled = newValue;
        });
    }
}

module.exports = indigoPreferredCompoundDetails;

