(function() {
    angular
        .module('indigoeln')
        .controller('IndigoProductBatchDetailsController', IndigoProductBatchDetailsController);

    /* @ngInject */
    function IndigoProductBatchDetailsController($scope, AppValues, InfoEditor, CalculationService, EntitiesBrowser,
                                                 $filter, ProductBatchSummaryOperations) {
        var vm = this;
        var grams = AppValues.getGrams();
        var liters = AppValues.getLiters();
        var moles = AppValues.getMoles();

        vm.showSummary = false;
        vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
        vm.detailTable = [];
        vm.selectControl = {};
        vm.saltCodeValues = AppValues.getSaltCodeValues();
        vm.model = vm.model || {};
        vm.selectBatch = selectBatch;
        vm.addNewBatch = addNewBatch;
        vm.duplicateBatch = duplicateBatch;
        vm.deleteBatch = deleteBatch;
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

        init();

        function init() {
            bindEvents();
        }

        function selectBatch(batch) {
            vm.onSelectBatch({batch: batch});
        }


        function checkEditDisabled() {
            return vm.isReadonly || !vm.selectedBatch || !vm.selectedBatch.nbkBatch;
        }


        function addNewBatch() {
            vm.batchOperation = ProductBatchSummaryOperations.addNewBatch().then(successAddedBatch);
        }

        function successAddedBatch(batch) {
            vm.onAddedBatch({batch: batch});
            vm.onChanged();
            selectBatch(batch);
        }

        function duplicateBatch() {
            vm.batchOperation = ProductBatchSummaryOperations.duplicateBatch(vm.selectedBatch).then(successAddedBatch);
        }

        function deleteBatch() {
            vm.onRemoveBatches({batches: [vm.selectedBatch]});
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
            vm.loading = ProductBatchSummaryOperations.registerBatches([vm.selectedBatch]);
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

        vm.productTableColumns = [
            {
                id: 'totalWeight',
                name: 'Total Weight',
                type: 'unit',
                width: '150px',
                unitItems: grams,
                onClose: function(data) {
                    CalculationService.setEntered(data);
                    CalculationService.calculateProductBatch(data);
                }
            },
            {
                id: 'totalVolume',
                name: 'Total Volume',
                type: 'unit',
                width: '150px',
                unitItems: liters,
                onClose: function(data) {
                    CalculationService.setEntered(data);
                    CalculationService.calculateProductBatch(data);
                }
            },
            {
                id: 'mol',
                name: 'Total Moles',
                type: 'unit',
                width: '150px',
                unitItems: moles,
                onClose: function(data) {
                    CalculationService.setEntered(data);
                    CalculationService.calculateProductBatch(data);
                }
            },
            {
                id: 'theoWeight',
                name: 'Theo. Wgt.',
                type: 'unit',
                unitItems: grams,
                width: '150px',
                hideSetValue: true,
                readonly: true
            },
            {
                id: 'theoMoles',
                name: 'Theo. Moles',
                width: '150px',
                type: 'unit',
                unitItems: moles,
                hideSetValue: true,
                readonly: true
            },
            {
                id: 'yield', name: '%Yield', type: 'primitive', sigDigits: 2
            },
            {
                id: 'registrationDate',
                name: 'Registration Date',
                format: function(val) {
                    return val ? $filter('date')(val, 'MMM DD, YYYY HH:mm:ss z') : null;
                }
            },
            {
                id: '$$registrationStatus',
                name: 'Registration Status',
                mark: function(batch) {
                    return batch.$$registrationStatus ? ('batch-status status-' + batch.$$registrationStatus.toLowerCase()) : '';
                }
            }
        ];

        function setStoicTable(table) {
            ProductBatchSummaryOperations.setStoicTable(table);
        }

        function onRowSelected(batch) {
            if (batch.stoichTable) {
                setStoicTable(batch.stoichTable);
            }

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
