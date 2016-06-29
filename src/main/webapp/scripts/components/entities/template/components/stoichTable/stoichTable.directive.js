/**
 * Created by Stepan_Litvinov on 3/10/2016.
 */
angular.module('indigoeln')
    .directive('stoichTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoichTable/stoichTable.html',
            controller: function ($scope, $rootScope, $http, $q, $uibModal, $log, AppValues, AlertModal, CalculationService) {
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
                    var calcData = data || {};
                    calcData.stoichTable = $scope.model.stoichTable;
                    calcData.actualProducts = actualProducts;
                    return calcData;
                }

                function fetchBatchByNbkNumber(nbkBatch, row) {
                    var searchRequest = {
                        advancedSearch: [{
                            condition: 'contains', field: 'fullNbkBatch', name: 'NBK batch #', value: nbkBatch
                        }],
                        databases: ['Indigo ELN']
                    };
                    $http.post('api/search/batch', searchRequest)
                        .then(function (result) {
                            var source = result.data[0];
                            if (source) {
                                _.extend(row, source.details);
                                row.rxnRole = row.rxnRole || {name: 'REACTANT'};
                            }
                        });
                }

                $scope.reactantsColumns = [
                    {
                        id: 'compoundId',
                        name: 'Compound ID',
                        type: 'input',
                        hasPopover: true
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
                        type: 'scalar'
                    },
                    {
                        id: 'weight',
                        name: 'Weight',
                        type: 'unit',
                        unitItems: grams
                    },
                    {
                        id: 'fullNbkBatch',
                        name: 'Nbk Batch #',
                        type: 'input',
                        hasPopover: true,
                        onClose: function (data) {
                            var row = data.row;
                            var nbkBatch = data.model;
                            fetchBatchByNbkNumber(nbkBatch, row);
                        }
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
                        type: 'scalar'
                    },
                    {
                        id: 'limiting',
                        name: 'Limiting',
                        type: 'boolean',
                        onClick: function (data) {
                            CalculationService.setEntered(data);
                            data = initDataForCalculation(data);
                            console.log(data);
                            CalculationService.recalculateStoichBasedOnBatch(data, false);
                        }
                    },
                    {
                        id: 'rxnRole',
                        name: 'Rxn Role',
                        type: 'select',
                        needOldVal: true,
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
                        type: 'scalar'
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
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'scalar'
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
                $scope.productsColumns = [
                    {
                        id: 'chemicalName', name: 'Chemical Name'
                    },
                    {
                        id: 'formula', name: 'Formula'
                    },
                    {
                        id: 'molWeight', name: 'Mol.Wt.', type: 'scalar', readonly: true
                    },
                    {
                        id: 'exactMass',
                        name: 'Exact Mass'
                    },
                    {
                        id: 'weight',
                        name: 'Theo. Wgt.',
                        type: 'unit',
                        unitItems: grams,
                        readonly: true
                    },
                    {
                        id: 'mol',
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
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'scalar'
                    },
                    {
                        id: 'hazardComments',
                        name: 'Hazard Comments'
                    },
                    {
                        id: 'eq',
                        name: 'EQ',
                        type: 'scalar'
                    }
                ];
                _.each($scope.reactantsColumns, function (column, i) {
                    var columnsToRecalculateStoic = ['molWeight', 'weight', 'volume', 'density', 'mol', 'eq',
                        'rxnRole', 'molarity', 'stoicPurity', 'loadFactor'];
                    var columnsToRecalculateSalt = ['saltCode', 'saltEq'];

                    if (_.contains(columnsToRecalculateStoic, column.id)) {
                        $scope.reactantsColumns[i] = _.extend(column, {
                            onClose: function (data) {
                                CalculationService.setEntered(data);
                                data = initDataForCalculation(data);
                                if (column.id === 'rxnRole') {
                                    if (data.model.name === 'SOLVENT') {
                                        var valuesToDefault = ['weight', 'mol', 'eq', 'density', 'stoicPurity'];
                                        CalculationService.resetValuesToDefault(valuesToDefault, data.row);
                                        CalculationService.setValuesReadonly(['weight', 'mol', 'eq', 'density'], data.row);
                                    } else if (data.model.name !== 'SOLVENT' && data.oldVal.name === 'SOLVENT') {
                                        CalculationService.resetValuesToDefault(['volume', 'molarity'], data.row);
                                        CalculationService.setValuesEditable(['weight', 'mol', 'eq', 'density'], data.row);
                                    }
                                }
                                console.log(data);
                                CalculationService.recalculateStoichBasedOnBatch(data, false);
                            }
                        });
                    } else if (_.contains(columnsToRecalculateSalt, column.id)) {
                        $scope.reactantsColumns[i] = _.extend(column, {
                            onClose: function (data) {
                                CalculationService.setEntered(data);
                                $scope.recalculateSalt(data.row);
                            }
                        });
                    }
                });
                $scope.clear = function () {
                    for (var key in $scope.selectedRow) {
                        if ($scope.selectedRow.hasOwnProperty(key) && !_.contains(['$$hashKey', 'selected'], key)) {
                            delete $scope.selectedRow[key];
                        }
                    }
                };
                $scope.appendRow = function () {
                    var reactant = CalculationService.createBatch($scope.model.stoichTable);
                    $scope.model.stoichTable.reactants.push(reactant);
                };
                $scope.removeRow = function () {
                    $scope.model.stoichTable.reactants = _.without($scope.model.stoichTable.reactants, $scope.selectedRow);
                    $rootScope.$broadcast('stoich-rows-changed');
                };
                $scope.onRowSelected = function (row) {
                    $scope.selectedRow = row || null;
                    $log.log(row);
                };
                $scope.recalculateSalt = function (reagent) {
                    function callback(result) {
                        reagent.molWeight = reagent.molWeight || {};
                        reagent.molWeight.value = result.data.molecularWeight;
                    }

                    CalculationService.recalculateSalt(reagent, callback);
                };

                function moleculeInfoResponseCallback(results) {
                    return _.map(results, function (result) {
                        return {
                            chemicalName: result.data.name,
                            formula: result.data.molecularFormula,
                            molWeight: {value: result.data.molecularWeight},
                            exactMass: result.data.exactMolecularWeight,
                            saltEq: {value: result.data.saltEq},
                            molecule: result.data.molecule
                        };
                    });
                }

                function getPromisesForMoleculeInfoRequest(reactionProperties, target) {
                    return _.map(reactionProperties.data[target], function (reactionProperty) {
                        return CalculationService.getMoleculeInfo(reactionProperty);
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
                        CalculationService.recalculateStoich(initDataForCalculation());
                    } else {
                        resetMolInfo();
                    }
                });
                $scope.$watch('share.actualProducts', function (products) {
                    actualProducts = products;
                }, true);

                $scope.$watch('model.stoichTable', function (stoichTable) {
                    $scope.share.stoichTable = stoichTable;
                }, true);

                var onNewStoichRows = $scope.$on('stoich-rows-changed', function (event, data) {
                    if (data) {
                        $scope.model.stoichTable.reactants = _.union($scope.model.stoichTable.reactants, data);
                    }
                    CalculationService.recalculateStoich(initDataForCalculation());
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
                    var batchesToSearch = [];
                    var stoicReactants = [];
                    _.each($scope.model.stoichTable.reactants, function (item) {
                        if (_.findWhere(item, {name: 'REACTANT'}) && item.structure) {
                            stoicReactants.push(item);
                        }
                    });
                    var isReactantAlreadyInStoic;
                    var allPromises = [];
                    _.each(reactionReactants, function (reactionReactant) {
                        var stoicAndReactionReactantsEqualityPromises = [];
                        _.each(stoicReactants, function (stoicReactant) {
                            stoicAndReactionReactantsEqualityPromises.push(isMoleculesEqual(stoicReactant.structure.molfile, reactionReactant.molecule));
                        });
                        allPromises.push($q.all(stoicAndReactionReactantsEqualityPromises).then(function () {
                            if (stoicAndReactionReactantsEqualityPromises.length) {
                                isReactantAlreadyInStoic = _.some(stoicAndReactionReactantsEqualityPromises, function (result) {
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
                    $q.all(allPromises).then(function () {
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