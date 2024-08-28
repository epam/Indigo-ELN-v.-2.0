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

require('./indigo-product-batch-details.less');
var template = require('./product-batch-details.html');

function indigoProductBatchDetails() {
    return {
        restrict: 'E',
        template: template,
        controller: IndigoProductBatchDetailsController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            batches: '=',
            experiment: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            reactants: '=',
            reactantsTrigger: '=',
            isExistStoichTable: '=',
            isReadonly: '=',
            saveExperimentFn: '&',
            batchOperation: '=',
            onAddedBatch: '&',
            onSelectBatch: '&',
            onRemoveBatches: '&',
            onChanged: '&'
        }
    };
}

IndigoProductBatchDetailsController.$inject = ['$scope', 'appValuesService', 'infoEditorService',
    'entitiesBrowserService', 'batchHelper'];

function IndigoProductBatchDetailsController($scope, appValuesService, infoEditorService, entitiesBrowserService,
                                             batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.productTableColumns = getDefaultColumns();
        vm.showSummary = false;
        vm.notebookId = entitiesBrowserService.getActiveTab().$$title;
        vm.saltCodeValues = appValuesService.getSaltCodeValues();
        vm.selectBatch = selectBatch;
        vm.editSolubility = editSolubility;
        vm.editResidualSolvents = editResidualSolvents;
        vm.editExternalSupplier = editExternalSupplier;
        vm.editMeltingPoint = editMeltingPoint;
        vm.editPurity = editPurity;
        vm.editHealthHazards = editHealthHazards;
        vm.editHandlingPrecautions = editHandlingPrecautions;
        vm.editStorageInstructions = editStorageInstructions;
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

    function editSolubility() {
        var callback = function(result) {
            vm.selectedBatch.solubility = result;
            vm.onChanged();
        };
        infoEditorService.editSolubility(vm.selectedBatch.solubility, callback);
    }

    function editResidualSolvents() {
        infoEditorService.editResidualSolvents(vm.selectedBatch.residualSolvents).then(function(result) {
            vm.selectedBatch.residualSolvents = result;
            vm.onChanged();
        });
    }

    function editExternalSupplier() {
        var callback = function(result) {
            vm.selectedBatch.externalSupplier = result;
            vm.onChanged();
        };
        infoEditorService.editExternalSupplier(vm.selectedBatch.externalSupplier, callback);
    }

    function editMeltingPoint() {
        var callback = function(result) {
            vm.selectedBatch.meltingPoint = result;
            vm.onChanged();
        };
        infoEditorService.editMeltingPoint(vm.selectedBatch.meltingPoint, callback);
    }

    function editPurity() {
        var callback = function(result) {
            vm.selectedBatch.purity = result;
            vm.onChanged();
        };
        infoEditorService.editPurity(vm.selectedBatch.purity, callback);
    }

    function editHealthHazards() {
        var callback = function(result) {
            vm.selectedBatch.healthHazards = result;
            vm.onChanged();
        };
        infoEditorService.editHealthHazards(vm.selectedBatch.healthHazards, callback);
    }

    function editHandlingPrecautions() {
        var callback = function(result) {
            vm.selectedBatch.handlingPrecautions = result;
            vm.onChanged();
        };
        infoEditorService.editHandlingPrecautions(vm.selectedBatch.handlingPrecautions, callback);
    }

    function editStorageInstructions() {
        var callback = function(result) {
            vm.selectedBatch.storageInstructions = result;
            vm.onChanged();
        };
        infoEditorService.editStorageInstructions(vm.selectedBatch.storageInstructions, callback);
    }

    function getDefaultColumns() {
        return [
            batchHelper.columns.totalWeight,
            batchHelper.columns.totalVolume,
            batchHelper.columns.totalMoles,
            batchHelper.columns.theoWeight,
            batchHelper.columns.theoMoles,
            batchHelper.columns.yield,
            batchHelper.columns.registrationDate,
            batchHelper.columns.registrationStatus
        ];
    }

    function bindEvents() {
        $scope.$watch(checkEditDisabled, function(newValue) {
            vm.isEditDisabled = newValue;
        });
    }
}

module.exports = indigoProductBatchDetails;
