/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
angular.module('indigoeln')
    .directive('productBatchDetails', function (InfoEditor, AppValues, CalculationService) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/productBatchDetails/productBatchDetails.html',
            controller: function ($scope, $uibModal, AlertModal) {
                $scope.model = $scope.model || {};
                $scope.model.productBatchDetails = {};
                $scope.detailTable = [];

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
                            console.log(data);
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
                            console.log(data);
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
                            console.log(data);
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
                    {id: 'yield', name: '%Yield', type: 'primitive', sigDigits: 2}
                ];

                var onBatchSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function (event, row) {
                    $scope.model.productBatchDetails = row;
                    $scope.detailTable[0] = row;
                });
                var onBatchSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', function () {
                    $scope.model.productBatchDetails = {};
                    $scope.detailTable[0] = {};
                });
                $scope.$on('$destroy', function () {
                    onBatchSummaryRowSelectedEvent();
                    onBatchSummaryRowDeselectedEvent();
                });

                $scope.editSolubility = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.solubility = result;
                    };
                    InfoEditor.editSolubility($scope.model.productBatchDetails.solubility, callback);
                };
                $scope.editResidualSolvents = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.residualSolvents = result;
                    };
                    InfoEditor.editResidualSolvents($scope.model.productBatchDetails.residualSolvents, callback);
                };
                $scope.editExternalSupplier = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.externalSupplier = result;
                    };
                    InfoEditor.editExternalSupplier($scope.model.productBatchDetails.externalSupplier, callback);
                };
                $scope.editMeltingPoint = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.meltingPoint = result;
                    };
                    InfoEditor.editMeltingPoint($scope.model.productBatchDetails.meltingPoint, callback);
                };
                $scope.editPurity = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.purity = result;
                    };
                    InfoEditor.editPurity($scope.model.productBatchDetails.purity, callback);
                };
                $scope.editHealthHazards = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.healthHazards = result;
                    };
                    InfoEditor.editHealthHazards($scope.model.productBatchDetails.healthHazards, callback);
                };
                $scope.editHandlingPrecautions = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.handlingPrecautions = result;
                    };
                    InfoEditor.editHandlingPrecautions($scope.model.productBatchDetails.handlingPrecautions, callback);
                };
                $scope.editStorageInstructions = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.storageInstructions = result;
                    };
                    InfoEditor.editStorageInstructions($scope.model.productBatchDetails.storageInstructions, callback);
                };
                $scope.registerBatch = function () {
                    AlertModal.info('not implemented yet');
                };
                $scope.recalculateSalt = function (reagent) {
                    function callback(result) {
                        var data = result.data;
                        reagent.molWeight = reagent.molWeight || {};
                        reagent.molWeight.value = data.molecularWeight;
                        reagent.formula = CalculationService.getSaltFormula(data);
                    }

                    CalculationService.recalculateSalt(reagent, callback);
                };
                $scope.$watch('share.stoichTable', function (stoichTable) {
                    if (stoichTable && stoichTable.reactants) {
                        $scope.model.productBatchDetails.precursors = _.filter(_.map(stoichTable.reactants, function (item) {
                            return item.compoundId || item.fullNbkBatch;
                        }), function (val) {
                            return !!val;
                        }).join(', ');
                    }
                }, true);
            }
        };
    });