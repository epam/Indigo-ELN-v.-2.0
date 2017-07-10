(function() {
    angular
        .module('indigoeln')
        .controller('IndigoProductBatchDetailsController', IndigoProductBatchDetailsController);

    /* @ngInject */
    function IndigoProductBatchDetailsController($scope, AppValues, InfoEditor, $timeout, CalculationService,
                                                 ProductBatchSummaryCache, $filter, StoichTableCache, $rootScope,
                                                 ProductBatchSummaryOperations, EntitiesBrowser) {

        var vm = this;
        var _batches;
        var grams = AppValues.getGrams();
        var liters = AppValues.getLiters();
        var moles = AppValues.getMoles();
        var stoichTable;
        var productBatches;

        vm.showSummary = false;
        vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
        vm.sourceValues = AppValues.getSourceValues();
        vm.sourceDetailExternal = AppValues.getSourceDetailExternal();
        vm.sourceDetailInternal = AppValues.getSourceDetailInternal();
        vm.detailTable = [];
        vm.selectControl = {};
        vm.saltCodeValues = AppValues.getSaltCodeValues();
        vm.model = vm.model || {};
        vm.share = vm.share || {};
        vm.model.productBatchSummary = vm.model.productBatchSummary || {};
        vm.share.stoichTable = StoichTableCache.getStoicTable();

        vm.initSelectedBatch = initSelectedBatch;
        vm.onSelectBatch = onSelectBatch;
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
            _batches = vm.model.productBatchSummary.batches || [];
            if (vm.model.productBatchSummary) {
                vm.model.productBatchSummary.batches = _batches;
                ProductBatchSummaryCache.setProductBatchSummary(_batches);
            }
            bindEvents();
        }

        function initSelectedBatch() {
            $timeout(function() {
                if (_batches && _batches.length > 0) {
                    vm.selectedBatch = _batches[0];
                    onSelectBatch();
                }
            }, 1000);
        }

        function onSelectBatch() {
            if (vm.share.selectedRow) {
                vm.share.selectedRow.$$selected = false;
            }
            var row = vm.share.selectedRow = vm.selectedBatch || null;
            if (row) {
                row.$$selected = true;
            }
            setProductBatchDetails(row);
            vm.detailTable[0] = row;
            checkOnlySelectedBatch();
            $rootScope.$broadcast('batch-summary-row-selected', {
                row: row
            });
        }

        function addNewBatch() {
            ProductBatchSummaryOperations.addNewBatch().then(function(batch) {
                vm.selectedBatch = batch;
                onSelectBatch();
            });
        }

        function duplicateBatch() {
            ProductBatchSummaryOperations.duplicateBatch().then(function(batch) {
                vm.selectedBatch = batch;
                onSelectBatch();
            });
        }

        function deleteBatches() {
            var batches = vm.model.productBatchSummary.batches;
            var ind = batches.indexOf(vm.selectedBatch) - 1;
            var deleted = ProductBatchSummaryOperations.deleteBatches();
            if (deleted > 0) {
                if (ind < 0) {
                    ind = 0;
                }
                if (batches.length > 0) {
                    vm.selectedBatch = batches[ind];
                    onSelectBatch();
                } else {
                    onRowDeSelected();
                }
            }
        }

        function isIntendedSynced() {
            var intended = ProductBatchSummaryOperations.getIntendedNotInActual();
            return intended ? !intended.length : true;
        }

        function syncWithIntendedProducts() {
            ProductBatchSummaryOperations.syncWithIntendedProducts().then(function(batch) {
                vm.selectedBatch = batch;
                onSelectBatch();
            });
        }

        function registerBatches() {
            vm.loading = ProductBatchSummaryOperations.registerBatches();
        }

        function importSDFile() {
            vm.importLoading = true;
            ProductBatchSummaryOperations.importSDFile(function() {
                vm.importLoading = false;
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
            var callback = function(result) {
                getProductBatchDetails().residualSolvents = result;
                vm.experimentForm.$setDirty();
            };
            InfoEditor.editResidualSolvents(getProductBatchDetails().residualSolvents, callback);
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

        function setProductBatchDetails(batch) {
            vm.model.productBatchDetails = batch;
        }

        function setStoicTable(table) {
            stoichTable = table;
            ProductBatchSummaryOperations.setStoicTable(stoichTable);
        }

        function setProductBatches(batches) {
            productBatches = batches;
        }

        function onRowSelected(data, noevent) {
            setProductBatchDetails(data.row);
            if (data.stoichTable) {
                setStoicTable(data.stoichTable);
            }
            if (data.actualProducts) {
                setProductBatches(data.actualProducts);
            }
            vm.detailTable[0] = data.row;
            vm.selectedBatch = data.row;
            checkOnlySelectedBatch();
            if (vm.selectControl.setSelection) {
                vm.selectControl.setSelection(data.row);
            }
            if (!noevent) {
                $rootScope.$broadcast('batch-summary-row-selected', data);
            }
        }

        function checkOnlySelectedBatch() {
            var batches = ProductBatchSummaryCache.getProductBatchSummary();
            if (!batches) {
                return;
            }
            batches.forEach(function(b) {
                b.select = false;
            });
            if (vm.selectedBatch) {
                vm.selectedBatch.select = true;
            }
        }

        function onRowDeSelected() {
            setProductBatchDetails({});
            vm.detailTable = [];
            vm.selectedBatch = null;
            vm.selectControl.unSelect();
        }

        function bindEvents() {
            var events = [];
            events.push($scope.$on('batch-summary-row-selected', function(event, data) {
                onRowSelected(data, true);
            }));

            events.push($scope.$on('batch-summary-row-deselected', onRowDeSelected));

            events.push($scope.$watch('share.stoichTable', function(stoichTable) {
                if (stoichTable && stoichTable.reactants && getProductBatchDetails()) {
                    getProductBatchDetails().precursors = _.filter(_.map(stoichTable.reactants, function(item) {
                        return item.compoundId || item.fullNbkBatch;
                    }), function(val) {
                        return !!val;
                    }).join(', ');
                }
            }, true));

            $scope.$on('$destroy', function() {
                _.each(events, function(event) {
                    event();
                });
            });
        }
    }
})();
