/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';
angular.module('indigoeln')
    .directive('productBatchDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/productBatchDetails/productBatchDetails.html',
            controller: function ($scope, $uibModal, AlertModal) {
                $scope.model = $scope.model || {};
                $scope.model.productBatchDetails = $scope.model.productBatchDetails || {};

                var onBatchSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function(event, row) {
                    $scope.initialValue = angular.copy($scope.model.productBatchDetails);
                    $scope.model.productBatchDetails = row;
                });
                var onBatchSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', function () {
                    $scope.model.productBatchDetails = $scope.initialValue;
                });
                $scope.$on('$destroy', function() {
                    onBatchSummaryRowSelectedEvent();
                    onBatchSummaryRowDeselectedEvent();
                });

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

                $scope.editPurity = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'EditPurityController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-purity/edit-purity.html',
                        resolve: {
                            data: function() {
                                return $scope.model.productBatchDetails.purity;
                            },
                            dictionary: function(Dictionary) {
                                return Dictionary.get({id: 'solventName'}).$promise;
                            }
                        }
                    }).result.then(function(result) {
                        $scope.model.productBatchDetails.purity = result;
                    });
                };

                var selectFromDictionary = function (dictionary, model, title, callback) {
                    $uibModal.open({
                        animation: true,
                        size: 'sm',
                        controller: 'SelectFromDictionaryController',
                        templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/select-from-dictionary/select-from-dictionary.html',
                        resolve: {
                            data: function() {
                                return model;
                            },
                            dictionary: function(Dictionary) {
                                return Dictionary.get({id: dictionary}).$promise;
                            },
                            title: function() {
                                return title;
                            }
                        }
                    }).result.then(function(result) {
                        callback(result);
                    });
                };

                $scope.editHealthHazards = function () {
                    var dictionary = 'healthHazards';
                    var model = $scope.model.productBatchDetails.healthHazards;
                    var title = 'Edit Health Hazards';
                    var callback = function(result) {
                        $scope.model.productBatchDetails.healthHazards = _.compact(result);
                    };
                    selectFromDictionary(dictionary, model, title, callback);
                };
                $scope.editHandlingPrecautions = function () {
                    var dictionary = 'handlingPrecautions';
                    var model = $scope.model.productBatchDetails.handlingPrecautions;
                    var title = 'Edit Handling Precautions';
                    var callback = function(result) {
                        $scope.model.productBatchDetails.handlingPrecautions = _.compact(result);
                    };
                    selectFromDictionary(dictionary, model, title, callback);
                };
                $scope.editStorageInstructions = function () {
                    var dictionary = 'storageInstructions';
                    var model = $scope.model.productBatchDetails.storageInstructions;
                    var title = 'Edit Storage Instructions';
                    var callback = function(result) {
                        $scope.model.productBatchDetails.storageInstructions = _.compact(result);
                    };
                    selectFromDictionary(dictionary, model, title, callback);
                };
                $scope.registerBatch = function () {
                    AlertModal.info('not implemented yet');
                };
                

            }
        };
    });