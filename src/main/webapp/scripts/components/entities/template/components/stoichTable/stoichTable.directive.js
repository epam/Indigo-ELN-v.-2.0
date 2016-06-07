/**
 * Created by Stepan_Litvinov on 3/10/2016.
 */
angular.module('indigoeln')
    .directive('stoichTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoichTable/stoichTable.html',
            controller: function ($scope, $rootScope, $http, $q, $uibModal, $log, AppValues, AlertModal, StoichCalculator) {
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
                var reactionReactants, actualProducts;

                function initDataForCalculation(data) {
                    data.stoichTable = $scope.model.stoichTable;
                    data.actualProducts = actualProducts;
                }

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
                        type: 'input'
                    },
                    {
                        id: 'weight',
                        name: 'Weight',
                        type: 'unit',
                        unitItems: grams
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
                        unitItems: liters
                    },
                    {
                        id: 'mol',
                        name: 'Mol',
                        type: 'unit',
                        unitItems: moles
                    },
                    {
                        id: 'eq',
                        name: 'EQ',
                        type: 'input'
                    },
                    {
                        id: 'limiting',
                        name: 'Limiting',
                        type: 'boolean'
                    },
                    {
                        id: 'rxnRole',
                        name: 'Rxn Role',
                        type: 'select',
                        values: function () {
                            return rxnValues;
                        }
                    },
                    {
                        id: 'density',
                        name: 'Density',
                        type: 'unit',
                        unitItems: density
                    },
                    {
                        id: 'molarity',
                        name: 'Molarity',
                        type: 'unit',
                        unitItems: molarity
                    },
                    {
                        id: 'stoicPurity',
                        name: 'Purity',
                        type: 'input'
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
                        type: 'input'
                    },
                    {
                        id: 'loadFactor',
                        name: 'Load Factor',
                        type: 'unit',
                        unitItems: loadFactorUnits
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
                _.each($scope.reactantsColumns, function (column, i) {
                    var columnsToRecalculateStoic = ['molWeight', 'weight', 'volume', 'density', 'mol', 'eq',
                        'limiting', 'rxnRole', 'molarity', 'stoicPurity', 'saltCode', 'saltEq', 'loadFactor'];

                    if (_.contains(columnsToRecalculateStoic, column.id)) {
                        $scope.reactantsColumns[i] = _.extend(column, {
                            onClose: function (data) {
                                initDataForCalculation(data);
                                console.log(data);
                                StoichCalculator.recalculateStoich(data, false);
                            }
                        });
                    }
                });
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
                        id: 'theoWeight',
                        name: 'Theo. Wgt.',
                        type: 'unit',
                        unitItems: grams,
                        readonly: true
                    },
                    {
                        id: 'theoMoles',
                        name: 'Theo. Moles',
                        type: 'unit',
                        unitItems: moles,
                        readonly: true
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
                            initDataForCalculation(data);
                            // StoichCalculator.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'input',
                        onChange: function (data) {
                            console.log(data);
                            initDataForCalculation(data);
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
                $scope.appendRow = function () {
                    $scope.model.stoichTable.reactants.push({});
                };
                $scope.removeRow = function () {
                    $scope.model.stoichTable.reactants = _.without($scope.model.stoichTable.reactants, $scope.selectedRow);
                };
                $scope.onRowSelected = function (row) {
                    $scope.selectedRow = row || null;
                    $log.log(row);
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
                            saltEq: result.data.saltEq,
                            molecule: result.data.molecule
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
                $scope.$watch('share.actualProducts', function (products) {
                    actualProducts = products;
                }, true);
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
                var onStoicTableRecalculated = $scope.$on('stoic-table-recalculated', function (event, data) {
                    var newReactants = data.stoicBatches;
                    var newProducts = data.intendedProducts;
                    if ($scope.model.stoichTable.reactants && newReactants.length === $scope.model.stoichTable.reactants.length) {
                        _.each($scope.model.stoichTable.reactants, function (reactant, i) {
                            _.extend(reactant, newReactants[i]);
                        });
                    }
                    if ($scope.model.stoichTable.products && newProducts.length === $scope.model.stoichTable.products.length) {
                        _.each($scope.model.stoichTable.products, function (product, i) {
                            _.extend(product, newProducts[i]);
                        });
                    }
                });
                $scope.$on('$destroy', function () {
                    onNewStoichRows();
                    onStoicTableRecalculated();
                });

                var isMoleculesEqual = function (molecule1, molecule2) {
                    return $http.put('api/calculations/molecule/equals', [molecule1, molecule2]);
                };

                var getMissingReactionReactantsInStoic = function (callback) {
                    var stoicReactants = [];
                    var batchesToSearch = [];
                    _.each($scope.model.stoichTable.reactants, function (item) {
                        if (_.isEqual(item.rxnRole, {name: 'REACTANT'}) && item.structure) {
                            stoicReactants.push(item);
                        }
                    });
                    var isReactantAlreadyInStoic;
                    var reactionReactantPromises = [];
                    _.each(reactionReactants, function (reactionReactant) {
                        var stoicReactantPromises = [];
                        _.each(stoicReactants, function (stoicReactant) {
                            stoicReactantPromises.push(isMoleculesEqual(stoicReactant.structure.molfile, reactionReactant.molecule));
                        });
                        reactionReactantPromises.push($q.all(stoicReactantPromises).then(function () {
                            if (stoicReactantPromises.length) {
                                isReactantAlreadyInStoic = _.some(stoicReactantPromises, function (result) {
                                    return !!result.$$state.value.data;
                                });
                            } else {
                                isReactantAlreadyInStoic = false;
                            }
                            if (!isReactantAlreadyInStoic) {
                                batchesToSearch.push(reactionReactant);
                            }
                        }));
                    });
                    $q.all(reactionReactantPromises).then(function () {
                        callback(batchesToSearch);
                    });
                };

                $scope.analyzeRxn = function () {
                    getMissingReactionReactantsInStoic(function (batchesToSearch) {
                        if (batchesToSearch.length) {
                            $uibModal.open({
                                animation: true,
                                size: 'lg',
                                controller: 'AnalyzeRxnController',
                                templateUrl: 'scripts/components/entities/template/components/common/analyze-rxn/analyze-rxn.html',
                                resolve: {
                                    reactants: function () {
                                        return _.pluck(batchesToSearch, 'formula');
                                    }
                                }
                            });
                        } else {
                            AlertModal.info('Stoichiometry is synchronized', 'sm');
                        }
                    });
                };

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
            }
        };
    })
;