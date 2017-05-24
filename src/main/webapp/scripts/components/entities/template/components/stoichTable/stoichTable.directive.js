/**
 * Created by Stepan_Litvinov on 3/10/2016.
 */
angular.module('indigoeln')
    .directive('stoichTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoichTable/stoichTable.html',
            controller: function ($scope, $rootScope, $http, $q, $uibModal, $log, AppValues, AlertModal, Alert, Dictionary,
                                  CalculationService, SearchService, RegistrationService, DialogService, StoichTableCache, $timeout) {
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

                var addStoicReactant = function (reactant) {
                    $scope.model.stoichTable.reactants.push(reactant);
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

                var populateFetchedBatch = function (row, source) {
                    row.$$populatedBatch = source;
                    _.extend(row, source);
                    row.rxnRole = row.rxnRole || AppValues.getRxnRoleReactant();
                    row.weight = null;
                    row.volume = null;
                    CalculationService.recalculateStoich();
                };

                var ftimeout;
                function fetchBatchByNbkNumber(nbkBatch, success) {
                    var searchRequest = {
                        advancedSearch: [{
                            condition: 'contains', field: 'fullNbkBatch', name: 'NBK batch #', value: nbkBatch
                        }],
                        databases: ['Indigo ELN']
                    };
                    $timeout.cancel(ftimeout);
                    ftimeout = $timeout(function() {
                        SearchService.search(searchRequest, function (result) {
                            success(result.slice(0, 5))
                        });
                    }, 500)
                }

                function getWord(dicts, dictName, wordName) {
                    var dict = _.find(dicts, function (dict) {
                        return dict.name === dictName;
                    });
                    if (dict) {
                        return _.find(dict.words, function (word) {
                            return word.name === wordName;
                        });
                    }
                }

                function convertCompoundsToBatches(compounds) {
                    var deferred = $q.defer();
                    Dictionary.all(function (dicts) {
                        var result = _.map(compounds, function (c) {
                            return {
                                chemicalName: c.chemicalName,
                                compoundId: c.compoundNo,
                                conversationalBatchNumber: c.conversationalBatchNo,
                                fullNbkBatch: c.batchNo,
                                casNumber: c.casNo,
                                structure: {
                                    structureType: 'molecule',
                                    molfile: c.structure
                                },
                                formula: c.formula,
                                stereoisomer: getWord(dicts, 'Stereoisomer Code', c.stereoisomerCode),
                                saltCode: _.find(saltCodeValues, function (sc) {
                                    return sc.regValue === c.saltCode;
                                }),
                                saltEq: {value: c.saltEquivs, entered: false},
                                comments: c.comment
                            };
                        });
                        var queries = [];
                        _.each(result, function (item) {
                            queries.push(CalculationService.getMoleculeInfo(item));
                        });
                        _.each(result, function (item) {
                            queries.push(CalculationService.getImageForStructure(item.structure.molfile, 'molecule'));
                        });
                        $q.all(queries).then(function (data) {
                            var i = 0;
                            for (i = 0; i < result.length; i++) {
                                var item = result[i];
                                var molInfo = data[i];
                                item.formula = molInfo.data.molecularFormula;
                                item.molWeight = item.molWeight || {};
                                item.molWeight.value = molInfo.data.molecularWeight;
                            }
                            for (i = 0; i < result.length; i++) {
                                result[i].structure.image = data[result.length + i].data.image;
                            }
                            deferred.resolve(result);
                        });
                    });
                    return deferred.promise;
                }

                function fetchBatchByCompoundId(compoundId, row) {
                    var searchRequest = {
                        compoundNo: compoundId
                    };
                    RegistrationService.compounds(searchRequest, function (result) {
                        result = result.slice(0, 20);
                        convertCompoundsToBatches(result).then(function (batches) {
                            if (batches.length === 1) {
                                populateFetchedBatch(row, batches[0]);
                            } else if (batches.length > 1) {
                            DialogService.structureValidation(batches, compoundId, function (selectedBatch) {
                                    populateFetchedBatch(row, selectedBatch);
                                });
                            } else {
                                alertCompoundWrongFormat()
                            }
                        });
                    });
                }
                var alertWrongFormat = function() {
                    Alert.error('Notebook batch number does not exist or in the wrong format- format should be "nbk. number-exp. number-batch number"')
                }
                var alertCompoundWrongFormat = function() {
                    Alert.error('Compound does not exist or in the wrong format')
                }
                $scope.reactantsColumns = [
                    {
                        id: 'compoundId',
                        name: 'Compound ID',
                        type: 'input',
                        hasStructurePopover: true,
                        onClose: function (data) {
                            var row = data.row;
                            var compoundId = data.model;
                            fetchBatchByCompoundId(compoundId, row);
                        }
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
                        hasStructurePopover: true,
                        hasPopup : true,
                        popItemClick : function(row, item) {
                            row.fullNbkBatch = item.details.fullNbkBatch;
                            populateFetchedBatch(row, item.details);
                        },
                        onChange : function(data) {
                            var row = data.row;
                            if (row) {
                                if (!row.$$fullNbkBatchOld && row.fullNbkImmutablePart) row.$$fullNbkBatchOld = data.oldVal;
                                var nbkBatch = data.model;
                                row.$$popItems = null;
                                row.$$populatedBatch = null;
                                fetchBatchByNbkNumber(nbkBatch, function(result) {
                                    if (result[0]) {
                                        row.$$popItems = result.map(function(r) { return {item : r, title : r.details.fullNbkBatch}})
                                    }
                                    else {
                                        alertWrongFormat()
                                    }
                                })
                            }
                        },
                        onClose: function (data) {
                            var row = data.row;
                            var nbkBatch = data.model;
                            var bid = row.fullNbkBatch || '';
                            if (!row.$$populatedBatch) {
                                bid = bid.replace(/[^0-9.-]/g, "").trim();
                                if (row.fullNbkBatch) {
                                    fetchBatchByNbkNumber(nbkBatch, function(result) {
                                        var pb = result[0];
                                        if (pb && pb.details.fullNbkBatch == row.fullNbkBatch) {
                                            populateFetchedBatch(row, pb.details);
                                        } else {
                                            alertWrongFormat()
                                            row.fullNbkBatch = row.$$fullNbkBatchOld;
                                        }
                                    });
                                } else {
                                    alertWrongFormat()
                                    row.fullNbkBatch = row.$$fullNbkBatchOld;
                                }
                            }
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
                        unitItems: moles
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
                            CalculationService.recalculateStoichBasedOnBatch(data);
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
                        id: 'chemicalName', name: 'Chemical Name', type: 'input', hasStructurePopover: true
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
                        isIntended: true,
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
                    var SOLVENT = AppValues.getRxnRoleSolvent().name;
                    if (data.model.name === SOLVENT) {
                        var valuesToDefault = ['weight', 'mol', 'eq', 'density', 'stoicPurity'];
                        CalculationService.resetValuesToDefault(valuesToDefault, data.row);
                        CalculationService.setValuesReadonly(['weight', 'mol', 'eq', 'density'], data.row);
                    } else if (data.model.name !== SOLVENT && data.oldVal === SOLVENT) {
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
                                if (column.id === 'rxnRole') {
                                    onRxnRoleChange(data);
                                }
                                CalculationService.recalculateStoichBasedOnBatch(data);
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
                    $scope.selectedRow.rxnRole = AppValues.getRxnRoleReactant();
                };
                $scope.appendRow = function () {
                    var reactant = CalculationService.createBatch(getStoicTable());
                    addStoicReactant(reactant);
                };
                $scope.removeRow = function () {
                    setStoicReactants(_.without(getStoicReactants(), $scope.selectedRow));
                    $rootScope.$broadcast('stoich-rows-changed');
                };
                $scope.onRowSelected = function (row) {
                    $scope.selectedRow = row || null;
                    $log.debug(row);
                };
                var recalculateSalt = function (reagent) {
                    CalculationService.recalculateSalt(reagent).then(function () {
                        CalculationService.recalculateAmounts({row: reagent});
                    });
                };

                function getDefaultChemicalName(index) {
                    return 'P' + index;
                }

                function moleculeInfoResponseCallback(results, isIntended) {
                    return _.map(results, function (result, index) {
                        var batch = AppValues.getDefaultBatch();
                        batch.chemicalName = isIntended ? getDefaultChemicalName(index) : result.data.name;
                        batch.formula = result.data.molecularFormula;
                        batch.molWeight = result.data.molecularWeight;
                        batch.exactMass = result.data.exactMolecularWeight;
                        batch.structure = {
                            image: result.data.image,
                            molfile: result.data.molecule,
                            structureType: 'molecule'
                        };
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
                                setIntendedProducts(moleculeInfoResponseCallback(results, true));
                                getStructureImagesForIntendedProducts();
                                CalculationService.recalculateStoich();
                            });
                            $q.all(reactantPromises).then(function (results) {
                                reactionReactants = moleculeInfoResponseCallback(results);
                                CalculationService.recalculateStoich();
                            });
                            }
                        }
                    );
                }

                var unbinds = [];
                unbinds.push($scope.$watch('share.reaction', function (newMolFile) {
                    if (newMolFile) {
                        getReactionProductsAndReactants(newMolFile);
                        CalculationService.recalculateStoich();
                    } else {
                        setIntendedProducts(null);
                    }
                }));
                unbinds.push($scope.$watch('share.actualProducts', function (products) {
                    actualProducts = products;
                }, true));

                unbinds.push($scope.$watch('model.stoichTable', function (stoichTable) {
                    _.each(stoichTable.products, function (batch) {
                        if (!batch.$$batchHash) {
                            batch.$$batchHash = batch.formula + batch.exactMass;
                        }
                    });
                    $scope.share.stoichTable = stoichTable;
                    StoichTableCache.setStoicTable(stoichTable);
                }, true));

                $scope.$on('$destroy', function () {
                    _.each(unbinds, function (unbind) {
                        unbind();
                    });
                });

                var onNewStoichRows = $scope.$on('stoich-rows-changed', function (event, data) {
                    if (data) {
                        setStoicReactants(_.union(getStoicReactants(), data));
                    }
                    CalculationService.recalculateStoich();
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
                        if (_.findWhere(item, AppValues.getRxnRoleReactant()) && item.structure) {
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

                $scope.noReactantsInStoic = function () {
                    var REACTANT = AppValues.getRxnRoleReactant().name;
                    var rxnRoleReactant = _.filter(getStoicReactants(), function (batch) {
                        return batch.rxnRole.name === REACTANT && batch.structure && batch.structure.molfile;
                    });
                    return rxnRoleReactant.length === 0;
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
                                        return _.map(batchesToSearch, function (batch) {
                                            var batchCopy = angular.copy(batch);
                                            CalculationService.getImageForStructure(batchCopy.structure.molfile, 'molecule', function (image) {
                                                batchCopy.structure.image = image;
                                            });
                                            return batchCopy;
                                        });
                                    }
                                }
                            });
                        } else {
                            AlertModal.info('Stoichiometry is synchronized', 'sm');
                        }
                    });
                };

                $scope.createRxn = function () {
                    var REACTANT = AppValues.getRxnRoleReactant().name;
                    var stoicReactantsMolfiles = _.compact(_.map(getStoicReactants(), function (batch) {
                        return batch.rxnRole.name === REACTANT && batch.structure.molfile;
                    }));
                    var intendedProductsMolfiles = _.compact(_.map(getIntendedProducts(), function (batch) {
                        return batch.structure.molfile;
                    }));
                    CalculationService.combineReactionComponents(stoicReactantsMolfiles, intendedProductsMolfiles).then(function (result) {
                        CalculationService.getImageForStructure(result.data.structure, 'reaction', function (image) {
                            $rootScope.$broadcast('new-reaction-scheme', {
                                image: image,
                                molfile: result.data.structure
                            });
                        });
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