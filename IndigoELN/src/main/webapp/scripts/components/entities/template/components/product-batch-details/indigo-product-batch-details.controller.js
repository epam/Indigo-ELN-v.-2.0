(function() {
    angular
        .module('indigoeln')
        .controller('IndigoProductBatchDetailsController', IndigoProductBatchDetailsController);

    /* @ngInject */
    function IndigoProductBatchDetailsController($scope, AppValues, InfoEditor, CalculationService, EntitiesBrowser,
                                                 batchHelper, ProductBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.productTableColumns = getDefaultColumns();
            vm.showSummary = false;
            vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
            vm.detailTable = [];
            vm.selectControl = {};
            vm.saltCodeValues = AppValues.getSaltCodeValues();
            vm.model = vm.model || {};
            vm.selectBatch = selectBatch;
            vm.duplicateBatch = duplicateBatch;
            vm.isIntendedSynced = isIntendedSynced;
            vm.syncWithIntendedProducts = syncWithIntendedProducts;
            vm.registerBatch = registerBatch;
            vm.importSDFile = importSDFile;
            vm.exportSDFile = exportSDFile;
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
            batchHelper.close(column, data);
        }

        function selectBatch(batch) {
            vm.onSelectBatch({batch: batch});
        }

        function checkEditDisabled() {
            return vm.isReadonly || !vm.selectedBatch || !vm.selectedBatch.nbkBatch || vm.selectedBatch.registrationStatus;
        }

        function successAddedBatch(batch) {
            vm.onAddedBatch({batch: batch});
            vm.onChanged();
            selectBatch(batch);
        }

        function duplicateBatch() {
            vm.batchOperation = ProductBatchSummaryOperations.duplicateBatch(vm.selectedBatch).then(successAddedBatch);
        }

        function isIntendedSynced() {
            var intended = ProductBatchSummaryOperations.getIntendedNotInActual();

            return intended ? !intended.length : true;
        }

        function syncWithIntendedProducts() {
            vm.batchOperation = ProductBatchSummaryOperations.syncWithIntendedProducts().then(function(batches) {
                if (batches.length) {
                    _.forEach(batches, function(batch) {
                        vm.onAddedBatch({batch: batch});
                    });
                    selectBatch(batches[0]);
                }
            });
        }

        function registerBatch() {
            vm.loading = vm.saveExperimentFn().then(function() {
                return ProductBatchSummaryOperations.registerBatches([vm.selectedBatch]);
            });
        }

        function importSDFile() {
            vm.batchOperation = ProductBatchSummaryOperations.importSDFile().then(function(batches) {
                _.forEach(batches, function(batch) {
                    vm.onAddedBatch({batch: batch});
                });
                selectBatch(batches[0]);
            });
        }

        function exportSDFile() {
            ProductBatchSummaryOperations.exportSDFile();
        }

        function editSolubility() {
            var callback = function(result) {
                vm.selectedBatch.solubility = result;
                vm.onChanged();
            };
            InfoEditor.editSolubility(vm.selectedBatch.solubility, callback);
        }

        function editResidualSolvents() {
            InfoEditor.editResidualSolvents(vm.selectedBatch.residualSolvents).then(function(result) {
                vm.selectedBatch.residualSolvents = result;
                vm.onChanged();
            });
        }

        function editExternalSupplier() {
            var callback = function(result) {
                vm.selectedBatch.externalSupplier = result;
                vm.onChanged();
            };
            InfoEditor.editExternalSupplier(vm.selectedBatch.externalSupplier, callback);
        }

        function editMeltingPoint() {
            var callback = function(result) {
                vm.selectedBatch.meltingPoint = result;
                vm.onChanged();
            };
            InfoEditor.editMeltingPoint(vm.selectedBatch.meltingPoint, callback);
        }

        function editPurity() {
            var callback = function(result) {
                vm.selectedBatch.purity = result;
                vm.onChanged();
            };
            InfoEditor.editPurity(vm.selectedBatch.purity, callback);
        }

        function editHealthHazards() {
            var callback = function(result) {
                vm.selectedBatch.healthHazards = result;
                vm.onChanged();
            };
            InfoEditor.editHealthHazards(vm.selectedBatch.healthHazards, callback);
        }

        function editHandlingPrecautions() {
            var callback = function(result) {
                vm.selectedBatch.handlingPrecautions = result;
                vm.onChanged();
            };
            InfoEditor.editHandlingPrecautions(vm.selectedBatch.handlingPrecautions, callback);
        }

        function editStorageInstructions() {
            var callback = function(result) {
                vm.selectedBatch.storageInstructions = result;
                vm.onChanged();
            };
            InfoEditor.editStorageInstructions(vm.selectedBatch.storageInstructions, callback);
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
            CalculationService.recalculateSalt(reagent).then(function() {
                CalculationService.recalculateStoich();
            });
        }

        function getDefaultColumns() {
            return [
                batchHelper.columns.totalWeight,
                batchHelper.columns.totalVolume,
                batchHelper.columns.mol,
                batchHelper.columns.theoWeight,
                batchHelper.columns.theoMoles,
                batchHelper.columns.yield,
                batchHelper.columns.registrationDate,
                batchHelper.columns.registrationStatus
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
            vm.isBatchLoading = completed;
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
})();
