/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
angular.module('indigoeln')
    .directive('productBatchDetails', function (InfoEditor, AppValues, $timeout,  CalculationService, ProductBatchSummaryCache, $filter, StoichTableCache, $rootScope, ProductBatchSummaryOperations) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/productBatchDetails/productBatchDetails.html',
            controller: function ($scope, $uibModal, AlertModal) {
                $scope.model = $scope.model || {};
                $scope.showSummary = false;
                $scope.model.productBatchDetails = $scope.model.productBatchDetails || {};
                $scope.share.stoichTable = StoichTableCache.getStoicTable();

                if($scope.model.productBatchSummary){
                    var _batches = $scope.model.productBatchSummary.batches || [];
                    $scope.model.productBatchSummary.batches = _batches;
                    ProductBatchSummaryCache.setProductBatchSummary(_batches);
                }
                $scope.init = function() {
                    $timeout(function() {
                        if (_batches && _batches.length > 0) {
                            $scope.selectedBatch = _batches[0];
                            $scope.onSelectBatch()
                        }
                    }, 1000)
                }
                $scope.onSelectBatch = function () {
                    if($scope.share.selectedRow){
                        $scope.share.selectedRow.$$selected = false;
                    }
                    var row = $scope.share.selectedRow = $scope.selectedBatch || null;
                    if (row) {
                        row.$$selected = true;
                    }
                    setProductBatchDetails(row);
                    $scope.detailTable[0] = row;
                    console.log('onSelectBatch', row)
                    checkOnlySelectedBatch()
                    $rootScope.$broadcast('batch-summary-row-selected', {row : row});
                };
                $scope.detailTable = [];
                $scope.selectControl = {};


                var grams = AppValues.getGrams();
                var liters = AppValues.getLiters();
                var moles = AppValues.getMoles();

                $scope.saltCodeValues = AppValues.getSaltCodeValues();
                $scope.productTableColumns = [
                    {
                        id: 'totalWeight',
                        name: 'Total Weight',
                        type: 'unit',
                        width: '150px',
                        unitItems: grams,
                        onClose: function (data) {
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
                        onClose: function (data) {
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
                        onClose: function (data) {
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
                    {id: 'yield', name: '%Yield', type: 'primitive', sigDigits: 2},
                    {
                        id: 'registrationDate', name: 'Registration Date', format: function (val) {
                        return val ? $filter('date')(val, 'MMM DD, YYYY HH:mm:ss z') : null;
                    }
                    },
                    {id: 'registrationStatus', name: 'Registration Status'}
                ];

                function getProductBatchDetails() {
                    return $scope.model.productBatchDetails;
                }

                function setProductBatchDetails(batch) {
                    $scope.model.productBatchDetails = batch;
                }

                var stoichTable, productBatches;

                function getStoicTable() {
                    return stoichTable;
                }

                function setStoicTable(table) {
                    stoichTable = table;
                    ProductBatchSummaryOperations.setStoicTable(stoichTable)
                }

                var getProductBatches = function () {
                    return productBatches;
                };
                var setProductBatches = function (batches) {
                    productBatches = batches;
                };
                var onRowSelected = function(data, noevent) {
                    setProductBatchDetails(data.row);
                    if (data.stoichTable)
                        setStoicTable(data.stoichTable);
                    if (data.actualProducts)
                        setProductBatches(data.actualProducts);
                    $scope.detailTable[0] = data.row;
                    $scope.selectedBatch = data.row;
                    checkOnlySelectedBatch()
                    if ($scope.selectControl.setSelection) $scope.selectControl.setSelection(data.row);
                    if (!noevent) {
                        $rootScope.$broadcast('batch-summary-row-selected', data);
                    }
                }
                function checkOnlySelectedBatch() {
                    var batches = ProductBatchSummaryCache.getProductBatchSummary();
                    batches.forEach(function(b) {
                        b.select = false;
                    })
                    if ($scope.selectedBatch) $scope.selectedBatch.select = true;
                }
                var onBatchSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function (event, data) {
                    onRowSelected(data, true)
                });
                function onRowDeSelected() {
                    setProductBatchDetails({});
                    $scope.detailTable = [];
                    $scope.selectedBatch = null;
                    $scope.selectControl.unSelect();
                }

                var onBatchSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', onRowDeSelected);

                $scope.$on('$destroy', function () {
                    onBatchSummaryRowSelectedEvent();
                    onBatchSummaryRowDeselectedEvent();
                });

                $scope.addNewBatch = function() {
                    ProductBatchSummaryOperations.addNewBatch().then(function(batch) {
                        $scope.selectedBatch = batch;
                        $scope.onSelectBatch()
                    });
                } 
                $scope.duplicateBatch = function() {
                    ProductBatchSummaryOperations.duplicateBatch().then(function(batch) {
                        $scope.selectedBatch = batch;
                        $scope.onSelectBatch()
                    })
                }  
                $scope.deleteBatches = function() {
                    var batches = $scope.model.productBatchSummary.batches;
                    var ind = batches.indexOf($scope.selectedBatch) - 1;
                    var deleted = ProductBatchSummaryOperations.deleteBatches()
                    if (deleted > 0) {
                        if (ind < 0) ind = 0;
                        if (batches.length > 0) {
                            $scope.selectedBatch = batches[ind];
                            $scope.onSelectBatch()
                        } else {
                            onRowDeSelected()
                        }
                    }
                } 
                $scope.isIntendedSynced = function () {
                    var intended = ProductBatchSummaryOperations.getIntendedNotInActual()
                    return intended ? !intended.length : true;
                };

                $scope.syncWithIntendedProducts = function () {
                    ProductBatchSummaryOperations.syncWithIntendedProducts().then(function(batch) {
                        $scope.selectedBatch = batch;
                        $scope.onSelectBatch()
                    })
                }

                $scope.registerBatches = function () {
                    $scope.loading = ProductBatchSummaryOperations.registerBatches()
                };

                $scope.importSDFile = function() {
                    ProductBatchSummaryOperations.importSDFile();
                };

                $scope.exportSDFile = function () {
                    ProductBatchSummaryOperations.exportSDFile()
                };
                
                $scope.editSolubility = function () {
                    var callback = function (result) {
                        getProductBatchDetails().solubility = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editSolubility(getProductBatchDetails().solubility, callback);
                };
                $scope.editResidualSolvents = function () {
                    var callback = function (result) {
                        getProductBatchDetails().residualSolvents = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editResidualSolvents(getProductBatchDetails().residualSolvents, callback);
                };
                $scope.editExternalSupplier = function () {
                    var callback = function (result) {
                        getProductBatchDetails().externalSupplier = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editExternalSupplier(getProductBatchDetails().externalSupplier, callback);
                };
                $scope.editMeltingPoint = function () {
                    var callback = function (result) {
                        getProductBatchDetails().meltingPoint = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editMeltingPoint(getProductBatchDetails().meltingPoint, callback);
                };
                $scope.editPurity = function () {
                    var callback = function (result) {
                        getProductBatchDetails().purity = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editPurity(getProductBatchDetails().purity, callback);
                };
                $scope.editHealthHazards = function () {
                    var callback = function (result) {
                        getProductBatchDetails().healthHazards = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editHealthHazards(getProductBatchDetails().healthHazards, callback);
                };
                $scope.editHandlingPrecautions = function () {
                    var callback = function (result) {
                        getProductBatchDetails().handlingPrecautions = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editHandlingPrecautions(getProductBatchDetails().handlingPrecautions, callback);
                };
                $scope.editStorageInstructions = function () {
                    var callback = function (result) {
                        getProductBatchDetails().storageInstructions = result;
                        $scope.experimentForm.$setDirty();
                    };
                    InfoEditor.editStorageInstructions(getProductBatchDetails().storageInstructions, callback);
                };
                $scope.recalculateSalt = function (reagent) {
                    CalculationService.recalculateSalt(reagent).then(function () {
                        CalculationService.recalculateStoich();
                    });
                };

                var unsubscribe = $scope.$watch('share.stoichTable', function (stoichTable) {
                    if (stoichTable && stoichTable.reactants) {
                        getProductBatchDetails().precursors = _.filter(_.map(stoichTable.reactants, function (item) {
                            return item.compoundId || item.fullNbkBatch;
                        }), function (val) {
                            return !!val;
                        }).join(', ');
                    }
                }, true);
                $scope.$on('$destroy', function () {
                    unsubscribe();
                });
            }
        };
    });