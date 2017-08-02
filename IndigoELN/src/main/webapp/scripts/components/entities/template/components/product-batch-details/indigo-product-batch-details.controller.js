(function() {
    angular
        .module('indigoeln')
        .controller('IndigoProductBatchDetailsController', IndigoProductBatchDetailsController);

    /* @ngInject */
    function IndigoProductBatchDetailsController($scope, AppValues, InfoEditor, CalculationService, EntitiesBrowser,
                                                 ProductBatchSummaryCache, $filter, ProductBatchSummaryOperations) {
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
        vm.model.productBatchSummary = vm.model.productBatchSummary || {};

        vm.selectBatch = selectBatch;
        vm.addNewBatch = addNewBatch;
        vm.duplicateBatch = duplicateBatch;
        vm.deleteBatches = deleteBatches;
        vm.isIntendedSynced = isIntendedSynced;
        vm.syncWithIntendedProducts = syncWithIntendedProducts;
        vm.registerBatches = registerBatches;
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
   
        init();

        function init() {
            if (vm.batches) {
                // TODO: move to productBathcSummary
                ProductBatchSummaryCache.setProductBatchSummary(vm.batches);
            }
            bindEvents();
        }

        function selectBatch(batch) {
            vm.model.productBatchDetails = batch;
            vm.onSelectBatch({batch: batch});
        }


        function checkEditDisabled() {
            return !getProductBatchDetails() || vm.isReadonly || !vm.batchSelected || !vm.batchSelected.nbkBatch;
        }


        function addNewBatch() {
            ProductBatchSummaryOperations.addNewBatch().then(successAddedBatch);
        }

        function successAddedBatch(batch) {
            vm.onAddedBatch({batch: batch});
            selectBatch(batch);
        }

        function duplicateBatch() {
            ProductBatchSummaryOperations.duplicateBatch(vm.selectedBatch).then(successAddedBatch);
        }

        function deleteBatches() {
            vm.onRemoveBatches({batches: [vm.selectedBatch]});
        }

        function isIntendedSynced() {
            var intended = ProductBatchSummaryOperations.getIntendedNotInActual();

            return intended ? !intended.length : true;
        }

        function syncWithIntendedProducts() {
            ProductBatchSummaryOperations.syncWithIntendedProducts().then(function(batches) {
                if (batches.length) {
                    _.forEach(batches, function(batch) {
                        vm.onAddedBatch({batch: batch});
                    });
                    selectBatch(batches[0]);
                }
            });
        }

        function registerBatches() {
            vm.loading = ProductBatchSummaryOperations.registerBatches();
        }

        function importSDFile() {
            vm.importLoading = true;
            ProductBatchSummaryOperations.importSDFile().then(function(batches) {
                vm.importLoading = false;
                _.forEach(batches, function(batch) {
                    vm.onAddedBatch({batch: batch});
                });
            });
        }

        function exportSDFile() {
            ProductBatchSummaryOperations.exportSDFile();
        }

        function editSolubility() {
            var callback = function(result) {
                getProductBatchDetails().solubility = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editSolubility(getProductBatchDetails().solubility, callback);
        }

        function editResidualSolvents() {
            InfoEditor.editResidualSolvents(getProductBatchDetails().residualSolvents).then(function(result) {
                getProductBatchDetails().residualSolvents = result;
                vm.experimentForm.$setDirty();
            });
        }

        function editExternalSupplier() {
            var callback = function(result) {
                getProductBatchDetails().externalSupplier = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editExternalSupplier(getProductBatchDetails().externalSupplier, callback);
        }

        function editMeltingPoint() {
            var callback = function(result) {
                getProductBatchDetails().meltingPoint = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editMeltingPoint(getProductBatchDetails().meltingPoint, callback);
        }

        function editPurity() {
            var callback = function(result) {
                getProductBatchDetails().purity = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editPurity(getProductBatchDetails().purity, callback);
        }

        function editHealthHazards() {
            var callback = function(result) {
                getProductBatchDetails().healthHazards = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editHealthHazards(getProductBatchDetails().healthHazards, callback);
        }

        function editHandlingPrecautions() {
            var callback = function(result) {
                getProductBatchDetails().handlingPrecautions = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editHandlingPrecautions(getProductBatchDetails().handlingPrecautions, callback);
        }

        function editStorageInstructions() {
            var callback = function(result) {
                getProductBatchDetails().storageInstructions = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editStorageInstructions(getProductBatchDetails().storageInstructions, callback);
        }

        function canEditSaltEq() {
            var o = vm.model.productBatchDetails;
            return o && o.saltCode && o.saltCode.value !== 0;
        }

        function recalculateSalt(reagent) {
            var o = vm.model.productBatchDetails;
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
                id: 'registrationStatus', name: 'Registration Status'
            }
        ];

        function getProductBatchDetails() {
            return vm.model.productBatchDetails;
        }

        function setStoicTable(table) {
            ProductBatchSummaryOperations.setStoicTable(table);
        }

        function setProductBatches(batches) {
            productBatches = batches;
        }

        function onRowSelected(batch) {
            if (batch.stoichTable) {
                setStoicTable(batch.stoichTable);
            }
            if (batch.actualProducts) {
                setProductBatches(batch.actualProducts);
            }
            vm.detailTable[0] = batch;
            vm.batchSelected = batch;
            vm.model.productBatchDetails = batch;
            if (vm.selectControl.setSelection) {
                vm.selectControl.setSelection(batch);
            }
        }

        function onRowDeSelected() {
            vm.detailTable = [];
            vm.batchSelected = null;
            vm.selectControl.unSelect();
        }

        function updateReactrants() {
            if (vm.model.productBatchDetails) {
                vm.model.productBatchDetails.precursors = _.filter(_.map(vm.reactants, function(item) {
                    return item.compoundId || item.fullNbkBatch;
                }), function(val) {
                    return !!val;
                }).join(', ');
            }
        }

        function bindEvents() {
            $scope.$watch('vm.selectedBatchTrigger', function() {
                vm.batchSelected = vm.selectedBatch;
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
