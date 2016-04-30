/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
angular.module('indigoeln')
    .directive('productBatchDetails', function (InfoEditor) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/productBatchDetails/productBatchDetails.html',
            controller: function ($scope, $uibModal, AlertModal) {
                $scope.model = $scope.model || {};
                $scope.model.productBatchDetails = $scope.model.productBatchDetails || {};

                var onBatchSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function (event, row) {
                    $scope.initialValue = angular.copy($scope.model.productBatchDetails);
                    $scope.model.productBatchDetails = row;
                });
                var onBatchSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', function () {
                    $scope.model.productBatchDetails = $scope.initialValue;
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
                        $scope.model.productBatchDetails.healthHazards = _.compact(result);
                    };
                    InfoEditor.editHealthHazards($scope.model.productBatchDetails.healthHazards, callback);
                };
                $scope.editHandlingPrecautions = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.handlingPrecautions = _.compact(result);
                    };
                    InfoEditor.editHandlingPrecautions($scope.model.productBatchDetails.handlingPrecautions, callback);
                };
                $scope.editStorageInstructions = function () {
                    var callback = function (result) {
                        $scope.model.productBatchDetails.storageInstructions = _.compact(result);
                    };
                    InfoEditor.editStorageInstructions($scope.model.productBatchDetails.storageInstructions, callback);
                };
                $scope.registerBatch = function () {
                    AlertModal.info('not implemented yet');
                };
            }
        };
    });