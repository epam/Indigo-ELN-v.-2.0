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
            model: '=',
            batches: '=',
            experiment: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            reactants: '=',
            reactantsTrigger: '=',
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

IndigoProductBatchDetailsController.$inject = ['$scope', 'appValuesService', 'infoEditorService', 'calculationService',
    'entitiesBrowserService', 'batchHelperService', 'productBatchSummaryOperations'];

function IndigoProductBatchDetailsController($scope, appValuesService, infoEditorService, calculationService,
                                             entitiesBrowserService, batchHelperService,
                                             productBatchSummaryOperations) {
    var vm = this;

    init();

    function init() {
        vm.productTableColumns = getDefaultColumns();
        vm.showSummary = false;
        vm.notebookId = entitiesBrowserService.getActiveTab().$$title;
        vm.detailTable = [];
        vm.selectControl = {};
        vm.saltCodeValues = appValuesService.getSaltCodeValues();
        vm.model = vm.model || {};
        vm.selectBatch = selectBatch;
        vm.registerBatch = registerBatch;
        vm.editSolubility = editSolubility;
        vm.editResidualSolvents = editResidualSolvents;
        vm.editExternalSupplier = editExternalSupplier;
        vm.editMeltingPoint = editMeltingPoint;
        vm.editPurity = editPurity;
        vm.editHealthHazards = editHealthHazards;
        vm.editHandlingPrecautions = editHandlingPrecautions;
        vm.editStorageInstructions = editStorageInstructions;
        vm.canEditSaltEq = canEditSaltEq;
        vm.recalculateSalt = recalculateSalt;
        vm.onBatchOperationChanged = onBatchOperationChanged;
        vm.isBatchLoading = false;
        vm.onClose = onClose;

        bindEvents();
    }

    function onClose(column, data) {
        batchHelperService.close(column, data);
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

    function registerBatch() {
        vm.loading = vm.saveExperimentFn().then(function() {
            return productBatchSummaryOperations.registerBatches([vm.selectedBatch]);
        });
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

    function canEditSaltEq() {
        var o = vm.selectedBatch;

        return o && o.saltCode && o.saltCode.value !== 0;
    }

    function recalculateSalt(reagent) {
        var o = vm.selectedBatch;
        if (o.saltCode.value === 0) {
            o.saltEq.value = 0;
        } else {
            o.saltEq.value = Math.abs(o.saltEq.value);
        }
        calculationService.recalculateSalt(reagent).then(function() {
            calculationService.recalculateStoich();
        });
    }

    function getDefaultColumns() {
        return [
            batchHelperService.columns.totalWeight,
            batchHelperService.columns.totalVolume,
            batchHelperService.columns.mol,
            batchHelperService.columns.theoWeight,
            batchHelperService.columns.theoMoles,
            batchHelperService.columns.yield,
            batchHelperService.columns.registrationDate,
            batchHelperService.columns.registrationStatus
        ];
    }

    function onRowSelected(batch) {
        vm.detailTable[0] = batch;
        vm.selectedBatch = batch;
        if (vm.selectControl.setSelection) {
            vm.selectControl.setSelection(batch);
        }
    }

    function onRowDeSelected() {
        vm.detailTable = [];
        vm.selectedBatch = null;
        vm.selectControl.unSelect();
    }

    function updateReactrants() {
        if (vm.selectedBatch) {
            vm.selectedBatch.precursors = _.filter(_.map(vm.reactants, function(item) {
                return item.compoundId || item.fullNbkBatch;
            }), function(val) {
                return !!val;
            }).join(', ');
        }
    }

    function onBatchOperationChanged(completed) {
        vm.isBatchLoading = !completed;
    }

    function bindEvents() {
        $scope.$watch('vm.selectedBatchTrigger', function() {
            if (vm.selectedBatch) {
                onRowSelected(vm.selectedBatch);
            } else {
                onRowDeSelected();
            }
        });

        $scope.$watch(checkEditDisabled, function(newValue) {
            vm.isEditDisabled = newValue;
        });

        $scope.$watch('vm.model.stoichTable', function() {
            vm.isExistStoichTable = !!vm.model.stoichTable;
        });

        $scope.$watch('vm.reactantsTrigger', function() {
            updateReactrants();
        });
    }
}

module.exports = indigoProductBatchDetails;
