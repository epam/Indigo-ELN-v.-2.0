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
                $scope.model.productBatchDetails.residualSolvents = $scope.model.productBatchDetails.residualSolvents || [];
                $scope.model.productBatchDetails.solubility = $scope.model.productBatchDetails.solubility || [];

                var solventsToString = function() {
                    var solvents = $scope.model.productBatchDetails.residualSolvents;
                    var solventStrings = _.map($scope.model.productBatchDetails.residualSolvents, function(solvent) {
                        return solvent.name.name ? solvent.eq + ' mols of ' + solvent.name.name : '';
                    });
                    return solventStrings.join(', ');
                };

                var solubilityToString = function() {
                    var solubilityStrings = _.map($scope.model.productBatchDetails.solubility, function(solubility) {
                        return solubility.eq + ' mols of ' + solubility.name.name;
                    });
                    return solubilityStrings.join(', ');
                };

                $scope.model.productBatchDetails.residualSolventsString = solventsToString();
                $scope.model.productBatchDetails.solubilityString = solubilityToString();


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
                    }).result.then(function(solubility) {
                        $scope.model.productBatchDetails.solubility = solubility;
                        $scope.model.productBatchDetails.solubilityString = solubilityToString();
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
                    }).result.then(function(solvents) {
                        $scope.model.productBatchDetails.residualSolvents = solvents;
                        $scope.model.productBatchDetails.residualSolventsString = solventsToString();
                    });
                };
            }
        };
    });