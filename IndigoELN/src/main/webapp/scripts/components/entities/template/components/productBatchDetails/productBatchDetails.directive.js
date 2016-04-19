/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
/* globals $ */
'use strict';
angular.module('indigoeln')
    .directive('productBatchDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/productBatchDetails/productBatchDetails.html',
            controller: function($scope, $uibModal){
                $scope.model = {};
                $scope.model.productBatchDetails = $scope.model.productBatchDetails || {};
                $scope.model.productBatchDetails.residualSolvents = $scope.model.productBatchDetails.residualSolvents || {};
                $scope.model.productBatchDetails.solubility = $scope.model.productBatchDetails.solubility || {};
                $scope.model.productBatchDetails.meltingPoint = $scope.model.productBatchDetails.meltingPoint || {};
                $scope.model.productBatchDetails.healthHazard = $scope.model.productBatchDetails.healthHazard || {};
                $scope.model.productBatchDetails.externalSupplier = $scope.model.productBatchDetails.externalSupplier || {};
                $scope.model.productBatchDetails.purity = $scope.model.productBatchDetails.purity || {};

                $scope.editSolubility = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'EditSolubilityController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-solubility/edit-solubility.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.solubility;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.solubility = result;
                    });
                };

                $scope.editResidualSolvents = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'EditResidualSolventsController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-residual-solvents/edit-residual-solvents.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.residualSolvents;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.residualSolvents = result;
                    });
                };

                $scope.editExternalSupplier = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'md',
                        controller: 'EditExternalSupplierController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-external-supplier/edit-external-supplier.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.externalSupplier;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.externalSupplier = result;
                    });
                };

                $scope.editPurity = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'EditPurityController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-purity/edit-purity.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.purity;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.purity = result;
                    });
                };

                $scope.editMeltingPoint = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'md',
                        controller: 'EditMeltingPointController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-melting-point/edit-melting-point.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.meltingPoint;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.meltingPoint = result;
                    });
                };

                $scope.editHealthHazard = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'EditHealthHazardController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-health-hazard/edit-health-hazard.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.healthHazard;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.healthHazard = result;
                    });
                };
            }
        };
    });