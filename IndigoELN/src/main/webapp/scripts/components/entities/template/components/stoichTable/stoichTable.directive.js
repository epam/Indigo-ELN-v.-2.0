/**
 * Created by Stepan_Litvinov on 3/10/2016.
 */
angular.module('indigoeln')
    .directive('stoichTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoichTable/stoichTable.html',
            controller: function ($scope, $rootScope, $http, $q, $uibModal, $log, AppValues, AlertModal, CalculationService, SearchService) {
                $scope.model = $scope.model || {};
                $scope.model.stoichTable = $scope.model.stoichTable || {};
                $scope.model.stoichTable.reactants = $scope.model.stoichTable.reactants || [];
                $scope.model.stoichTable.products = $scope.model.stoichTable.products || [];

                var getStoicTable = function () {
                    return $scope.model.stoichTable;
                };
                var getStoicReactants = function () {
                    return $scope.model.stoichTable.reactants;
                };
                var getIntendedProducts = function () {
                    return $scope.model.stoichTable.products;
                };

                var setStoicReactants = function (reactants) {
                    $scope.model.stoichTable.reactants = reactants;
                };
                var setIntendedProducts = function (products) {
                    $scope.model.stoichTable.products = products;
                };

                var grams = AppValues.getGrams();
                var liters = AppValues.getLiters();
                var moles = AppValues.getMoles();
                var density = AppValues.getDensity();
                var molarity = AppValues.getMolarity();
                var rxnValues = AppValues.getRxnValues();
                var saltCodeValues = AppValues.getSaltCodeValues();
                var loadFactorUnits = AppValues.getLoadFactorUnits();
                var reactionReactants, actualProducts;

                function initDataForCalculation(data) {
                    var calcData = data || {};
                    calcData.stoichTable = getStoicTable();
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
                    SearchService.search(searchRequest, function (result) {
                        var source = result[0];
                        if (source) {
                            _.extend(row, source.details);
                            row.rxnRole = row.rxnRole || {name: 'REACTANT'};
                            CalculationService.recalculateStoich(initDataForCalculation());
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
                        name: 'CAS Number',
                        type: 'input'
                    },
                    {
                        id: 'chemicalName',
                        name: 'Chemical Name',
                        type: 'input'
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
                        id: 'volume',
                        name: 'Volume',
                        type: 'unit',
                        unitItems: liters
                    },
                    {
                        id: 'mol',
                        name: 'Mol',
                        type: 'unit',
                        unitItems: moles,
                        sigDigits: 2
                    },
                    {
                        id: 'eq',
                        name: 'EQ',
                        type: 'scalar',
                        sigDigits: 2
                    },
                    {
                        id: 'limiting',
                        name: 'Limiting',
                        type: 'boolean',
                        onClick: function (data) {
                            CalculationService.setEntered(data);
                            data = initDataForCalculation(data);
                            $log.log(data);
                            CalculationService.recalculateStoichBasedOnBatch(data, false);
                        }
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
                        type: 'scalar'
                    },
                    {
                        id: 'formula',
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
                        name: 'Exact Mass',
                        type: 'primitive'
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
                        },
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            recalculateSalt(data.row);
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'scalar',
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            recalculateSalt(data.row);
                        }
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
                function onRxnRoleChange(data) {
                    if (data.model.name === 'SOLVENT') {
                        var valuesToDefault = ['weight', 'mol', 'eq', 'density', 'stoicPurity'];
                        CalculationService.resetValuesToDefault(valuesToDefault, data.row);
                        CalculationService.setValuesReadonly(['weight', 'mol', 'eq', 'density'], data.row);
                    } else if (data.model.name !== 'SOLVENT' && data.oldVal === 'SOLVENT') {
                        CalculationService.resetValuesToDefault(['volume', 'molarity'], data.row);
                        CalculationService.setValuesEditable(['weight', 'mol', 'eq', 'density'], data.row);
                    }
                }

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
                                    onRxnRoleChange(data);
                                }
                                $log.log(data);
                                CalculationService.recalculateStoichBasedOnBatch(data, false);
                            }
                        });
                    } else if (_.contains(columnsToRecalculateSalt, column.id)) {
                        $scope.reactantsColumns[i] = _.extend(column, {
                            onClose: function (data) {
                                CalculationService.setEntered(data);
                                recalculateSalt(data.row);
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
                    $scope.selectedRow.rxnRole = {name: 'REACTANT'};
                };
                $scope.appendRow = function () {
                    var reactant = CalculationService.createBatch(getStoicTable());
                    getStoicReactants().push(reactant);
                };
                $scope.removeRow = function () {
                    setStoicReactants(_.without(getStoicReactants(), $scope.selectedRow));
                    $rootScope.$broadcast('stoich-rows-changed');
                };
                $scope.onRowSelected = function (row) {
                    $scope.selectedRow = row || null;
                    $log.log(row);
                };
                var recalculateSalt = function (reagent) {
                    function callback(result) {
                        var data = result.data;
                        data.saltEq = reagent.saltEq;
                        reagent.molWeight = reagent.molWeight || {};
                        reagent.molWeight.value = data.molecularWeight;
                        reagent.formula = CalculationService.getSaltFormula(data);
                        CalculationService.recalculateStoich(initDataForCalculation());
                    }
                    CalculationService.recalculateSalt(reagent, callback);
                };

                function moleculeInfoResponseCallback(results) {
                    return _.map(results, function (result) {
                        var batch = AppValues.getDefaultBatch();
                        batch.chemicalName = result.data.name;
                        batch.formula = result.data.molecularFormula;
                        batch.molWeight = result.data.molecularWeight;
                        batch.exactMass = result.data.exactMolecularWeight;
                        batch.structure = {image: result.data.image, molfile: result.data.molecule};
                        return batch;
                    });
                }

                function getPromisesForMoleculeInfoRequest(reactionProperties, target) {
                    return _.map(reactionProperties.data[target], function (reactionProperty) {
                        return CalculationService.getMoleculeInfo(reactionProperty);
                    });
                }

                function getStructureImagesForIntendedProducts() {
                    _.each(getIntendedProducts(), function (item) {
                        CalculationService.getImageForStructure(item.structure.molfile, 'molecule', function (image) {
                            item.structure.image = image;
                        });
                    });
                }

                function getReactionProductsAndReactants(molFile) {
                    $http.put('api/calculations/reaction/extract', molFile).then(function (reactionProperties) {
                        if (reactionProperties.data.products && reactionProperties.data.products.length) {
                            var productPromises = getPromisesForMoleculeInfoRequest(reactionProperties, 'products');
                            var reactantPromises = getPromisesForMoleculeInfoRequest(reactionProperties, 'reactants');
                            $q.all(productPromises).then(function (results) {
                                setIntendedProducts(moleculeInfoResponseCallback(results));
                                // getStructureImagesForIntendedProducts();
                                CalculationService.recalculateStoich(initDataForCalculation());
                            });
                            $q.all(reactantPromises).then(function (results) {
                                reactionReactants = moleculeInfoResponseCallback(results);
                                CalculationService.recalculateStoich(initDataForCalculation());
                            });
                            }
                        }
                    );
                }

                $scope.$watch('share.reaction', function (newMolFile) {
                    if (newMolFile) {
                        getReactionProductsAndReactants(newMolFile);
                        CalculationService.recalculateStoich(initDataForCalculation());
                    }
                });
                $scope.$watch('share.actualProducts', function (products) {
                    actualProducts = products;
                }, true);

                $scope.$watch('model.stoichTable', function (stoichTable) {
                    _.each(stoichTable.products, function (item) {
                        if (!item.$$batchHash) {
                            item.$$batchHash = +new Date() + Math.random();
                        }
                    });
                    $scope.share.stoichTable = stoichTable;
                }, true);

                var onNewStoichRows = $scope.$on('stoich-rows-changed', function (event, data) {
                    if (data) {
                        setStoicReactants(_.union(getStoicReactants(), data));
                    }
                    CalculationService.recalculateStoich(initDataForCalculation());
                });
                var onStoicTableRecalculated = $scope.$on('stoic-table-recalculated', function (event, data) {
                    var newReactants = data.stoicBatches;
                    var newProducts = data.intendedProducts;
                    if (getStoicReactants() && newReactants.length === getStoicReactants().length) {
                        _.each(getStoicReactants(), function (reactant, i) {
                            _.extend(reactant, newReactants[i]);
                        });
                    }
                    if (getIntendedProducts() && newProducts.length === getIntendedProducts().length) {
                        _.each(getIntendedProducts(), function (product, i) {
                            _.extend(product, newProducts[i]);
                        });
                    }
                });
                $scope.$on('$destroy', function () {
                    onNewStoichRows();
                    onStoicTableRecalculated();
                });

                var getMissingReactionReactantsInStoic = function (callback) {
                    var batchesToSearch = [];
                    var stoicReactants = [];
                    _.each(getStoicReactants(), function (item) {
                        if (_.findWhere(item, {name: 'REACTANT'}) && item.structure) {
                            stoicReactants.push(item);
                        }
                    });
                    var isReactantAlreadyInStoic;
                    var allPromises = [];
                    _.each(reactionReactants, function (reactionReactant) {
                        var stoicAndReactionReactantsEqualityPromises = [];
                        _.each(stoicReactants, function (stoicReactant) {
                            stoicAndReactionReactantsEqualityPromises.push(CalculationService.isMoleculesEqual(stoicReactant.structure.molfile, reactionReactant.structure.molfile));
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