/**
 * Created by Stepan_Litvinov on 3/10/2016.
 */
angular.module('indigoeln')
    .directive('stoichTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoichTable/stoichTable.html',
            controller: function ($scope, $rootScope, $http, $q, $uibModal, AppValues, StoichCalculator) {
                $scope.model = $scope.model || {};
                $scope.model.stoichTable = $scope.model.stoichTable || {};
                $scope.model.stoichTable.reactants = $scope.model.stoichTable.reactants || [];
                $scope.model.stoichTable.products = $scope.model.stoichTable.products || [];

                var grams = AppValues.getGrams();
                var liters = AppValues.getLiters();
                var density = AppValues.getDensity();
                var moles = AppValues.getMoles();
                var molarity = AppValues.getMolarity();
                var rxnValues = AppValues.getRxnValues();
                var saltCodeValues = AppValues.getSaltCodeValues();
                var loadFactorUnits = AppValues.getLoadFactorUnits();
                var reactionReactants;

                $scope.reactantsColumns = [
                    {
                        id: 'compoundId',
                        name: 'Compound ID',
                        type: 'input'
                    },
                    {
                        id: 'casNumber',
                        name: 'CAS Number'
                    },
                    {
                        id: 'chemicalName',
                        name: 'Chemical Name',
                        type: 'input'
                    },
                    {
                        id: 'molWeight',
                        name: 'Mol Weight',
                        type: 'input',
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'weight',
                        name: 'Weight',
                        type: 'unit',
                        unitItems: grams,
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'nbkBatch',
                        name: 'Nbk Batch #',
                        type: 'input'
                    },
                    {
                        id: 'volume',
                        name: 'Volume',
                        type: 'unit',
                        unitItems: liters,
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'mol',
                        name: 'Mol',
                        unitItems: moles,
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'eq',
                        name: 'EQ',
                        type: 'input',
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'limiting',
                        name: 'Limiting',
                        type: 'boolean',
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'rxnRole',
                        name: 'Rxn Role',
                        type: 'select',
                        values: function () {
                            return rxnValues;
                        },
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'density',
                        name: 'Density',
                        type: 'unit',
                        unitItems: density,
                        onChange: function (data) {
                            console.log(data);
                            data.stoichTable = $scope.model.stoichTable;
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'molarity',
                        name: 'Molarity',
                        type: 'unit',
                        unitItems: molarity,
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateMolarAmountForSolvent(data);
                        }
                    },
                    {
                        id: 'purity',
                        name: 'Purity',
                        type: 'input',
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'molFormula',
                        name: 'Mol Formula',
                        type: 'input'
                    },
                    {
                        id: 'saltCode',
                        name: 'Salt Code',
                        type: 'select',
                        values: function () {
                            return saltCodeValues;
                        },
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'input',
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'loadFactor',
                        name: 'Load Factor',
                        type: 'unit',
                        unitItems: loadFactorUnits,
                        onChange: function (data) {
                            data.stoichTable = $scope.model.stoichTable;
                            console.log(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'hazardComments',
                        name: 'Hazard Comments',
                        type: 'input'
                    },
                    {
                        id: 'comments',
                        name: 'Comments',
                        type: 'input'
                    }
                ];
                $scope.productsColumns = [
                    {
                        id: 'chemicalName', name: 'Chemical Name'
                    },
                    {
                        id: 'formula', name: 'Formula'
                    },
                    {
                        id: 'molWeight', name: 'Mol.Wt.'
                    },
                    {
                        id: 'exactMass',
                        name: 'Exact Mass'
                    },
                    {
                        id: 'theoWgt',
                        name: 'Theo. Wgt.'
                    },
                    {
                        id: 'theoMoles',
                        name: 'Theo. Moles'
                    },
                    {
                        id: 'saltCode',
                        name: 'Salt Code',
                        type: 'select',
                        values: function () {
                            return saltCodeValues;
                        },
                        onChange: function (data) {
                            console.log(data);
                            data.stoichTable = $scope.model.stoichTable;
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'input',
                        onChange: function (data) {
                            console.log(data);
                            data.stoichTable = $scope.model.stoichTable;
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'hazardComments',
                        name: 'Hazard Comments'
                    },
                    {
                        id: 'eq',
                        name: 'EQ',
                        type: 'input'
                    }
                ];
                $scope.clear = function () {
                    for (var key in $scope.selectedRow) {
                        if ($scope.selectedRow.hasOwnProperty(key) && !_.contains(['$$hashKey', 'selected'], key)) {
                            delete $scope.selectedRow[key];
                        }
                    }
                };
                $scope.removeRow = function () {
                    $scope.rows = _.without($scope.rows, $scope.selectedRow);
                };
                $scope.onRowSelected = function (row) {
                    $scope.selectedRow = row || null;
                };
                $scope.recalculateSalt = function (reagent) {
                    var config = {
                        params: {
                            saltCode: reagent.saltCode ? reagent.saltCode.value : null,
                            saltEq: reagent.saltEq
                        }
                    };
                    $http.put('api/calculations/molecule/info', reagent.structure.molfile, config)
                        .then(function (result) {
                                reagent.molWeight = result.data.molecularWeight;
                            }
                        );
                };

                function moleculeInfoResponseCallback(results) {
                    return _.map(results, function (result) {
                        return {
                            chemicalName: result.data.name,
                            formula: result.data.molecularFormula,
                            molWeight: result.data.molecularWeight,
                            exactMass: result.data.exactMolecularWeight,
                            saltEq: result.data.saltEq
                        };
                    });
                }

                function getPromisesForMoleculeInfoRequest(reactionProperties, target) {
                    return _.map(reactionProperties.data[target], function (reactionProperty) {
                        var config = {
                            params: {
                                saltCode: reactionProperty.saltCode ? reactionProperty.saltCode.value : null,
                                saltEq: reactionProperty.saltEq
                            }
                        };
                        return $http.put('api/calculations/molecule/info', reactionProperty, config);
                    });
                }

                function getReactionProductsAndReactants(molFile) {
                    $http.put('api/calculations/reaction/extract', molFile).then(function (reactionProperties) {
                        if (reactionProperties.data.products && reactionProperties.data.products.length) {
                            var productPromises = getPromisesForMoleculeInfoRequest(reactionProperties, 'products');
                            var reactantPromises = getPromisesForMoleculeInfoRequest(reactionProperties, 'reactants');
                            $q.all(productPromises).then(function (results) {
                                $scope.model.stoichTable.products = moleculeInfoResponseCallback(results);

                            });
                            $q.all(reactantPromises).then(function (results) {
                                reactionReactants = moleculeInfoResponseCallback(results);
                            });
                            }
                        }
                    );
                }

                $scope.$watch('share.reaction', function (newMolFile) {
                    var resetMolInfo = function () {
                        $scope.model.stoichTable.products = null;
                    };
                    if (newMolFile) {
                        getReactionProductsAndReactants(newMolFile);
                    } else {
                        resetMolInfo();
                    }
                });
                $scope.$watch('model.stoichTable.reactants', function (newRows) {
                    _.each(newRows, function (row) {
                        if (row.saltCode || row.saltEq) {
                            $scope.recalculateSalt(row);
                        }
                    });
                }, true);

                // $scope.$watch('model.stoichTable.products', function (newRows) {
                //     _.each(newRows, function (row) {
                //         if (row.saltCode || row.saltEq) {
                //             $scope.recalculateSalt(row);
                //         }
                //     });
                // }, true);

                var onNewStoichRows = $scope.$on('new-stoich-rows', function (event, data) {
                    $scope.model.stoichTable.reactants = _.union($scope.model.stoichTable.reactants, data);
                });
                $scope.$on('$destroy', function () {
                    onNewStoichRows();
                });

                $scope.searchReagents = function (activeTab) {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'SearchReagentsController',
                        templateUrl: 'scripts/components/entities/template/components/common/search-reagents/search-reagents.html',
                        resolve: {
                            activeTab: function () {
                                return activeTab;
                            }
                        }
                    });
                };
                $scope.analyzeRxn = function () {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'AnalyzeRxnController',
                        templateUrl: 'scripts/components/entities/template/components/common/analyze-rxn/analyze-rxn.html',
                        resolve: {
                            reactants: function () {
                                return _.pluck(reactionReactants, 'formula');
                            }
                        }
                    });
                };
            }
        };
    })
;