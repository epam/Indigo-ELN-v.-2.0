/**
 * Created by Stepan_Litvinov on 3/2/2016.
 */
angular.module('indigoeln')
    .controller('ProductBatchSummaryController',
        function ($scope, $rootScope, $uibModal, $http, $stateParams, $q, $filter, InfoEditor, EntitiesBrowser, AlertModal, AppValues, CalculationService, RegistrationService) {
            $scope.model = $scope.model || {};
            $scope.model.productBatchSummary = $scope.model.productBatchSummary || {};
            $scope.model.productBatchSummary.batches = $scope.model.productBatchSummary.batches || [];
            var grams = AppValues.getGrams();
            var liters = AppValues.getLiters();
            var moles = AppValues.getMoles();
            var compoundValues = AppValues.getCompoundValues();
            var saltCodeValues = AppValues.getSaltCodeValues();
            var stereoisomerValues = AppValues.getStereoisomerValues();
            var sourceValues = AppValues.getSourceValues();
            var sourceDetailExternal = AppValues.getSourceDetailExternal();
            var sourceDetailInternal = AppValues.getSourceDetailInternal();
            var compoundProtectionValues = AppValues.getCompoundProtectionValues();
            var stoichTable;
            var unbinds = [];
            var setSelectSourceValueAction = {
                action: function () {
                    var that = this;
                    $uibModal.open({
                        templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary-set-source.html',
                        controller: 'ProductBatchSummarySetSourceController',
                        size: 'sm',
                        resolve: {
                            name: function () {
                                return that.title;
                            },
                            sourceValues: function () {
                                return sourceValues;
                            },
                            sourceDetailExternal: function () {
                                return sourceDetailExternal;
                            },
                            sourceDetailInternal: function () {
                                return sourceDetailInternal;
                            }
                        }
                    }).result.then(function (result) {
                        _.each($scope.model.productBatchSummary.batches, function (row) {
                            row.source = result.source;
                            row.sourceDetail = result.sourceDetail;
                        });
                    }, function () {

                    });
                }
            };

            unbinds.push($scope.$watch('model.productBatchSummary.batches', function (batches) {
                _.each(batches, function (batch) {
                    batch.$$purity = batch.purity ? batch.purity.asString : null;
                    batch.$$externalSupplier = batch.externalSupplier ? batch.externalSupplier.asString : null;
                    batch.$$meltingPoint = batch.meltingPoint ? batch.meltingPoint.asString : null;
                    batch.$$healthHazards = batch.healthHazards ? batch.healthHazards.asString : null;
                });
                $scope.share.actualProducts = batches;
            }, true));

            unbinds.push($scope.$watch('share.stoichTable', function (table) {
                stoichTable = table;
            }, true));

            $scope.columns = [
                {
                    id: 'structure',
                    name: 'Structure',
                    type: 'image',
                    isVisible: false,
                    width: '300px'
                },
                {
                    id: 'nbkBatch',
                    name: 'Nbk Batch #'
                },
                {
                    id: 'select',
                    name: 'Select',
                    type: 'boolean',
                    actions: [
                        {
                            name: 'Select All',
                            action: function () {
                                _.each($scope.model.productBatchSummary.batches, function (row) {
                                    row.select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function () {
                                _.each($scope.model.productBatchSummary.batches, function (row) {
                                    row.select = false;
                                });
                            }
                        }
                    ]
                },
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
                {id: 'yield', name: '%Yield', type: 'primitive', sigDigits: 2},
                {
                    id: 'compoundState',
                    name: 'Compound State',
                    type: 'select',
                    values: function () {
                        return compoundValues;
                    }
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
                    id: '$$purity',
                    realId: 'purity',
                    name: 'Purity',
                    type: 'popup',
                    openPopup: function (data) {
                        InfoEditor.editPurity(data.row.purity, function (result) {
                            data.row.purity = result;
                        });
                    }
                },
                {
                    id: '$$meltingPoint',
                    realId: 'meltingPoint',
                    name: 'Melting Point',
                    type: 'popup',
                    openPopup: function (data) {
                        InfoEditor.editMeltingPoint(data.row.meltingPoint, function (result) {
                            data.row.meltingPoint = result;
                        });
                    }
                },
                {
                    id: 'molWeight', name: 'Mol Wgt', type: 'scalar'
                },
                {
                    id: 'formula', name: 'Mol Formula', type: 'input', readonly: true
                },
                {
                    id: 'conversationalBatchNumber', name: 'Conversational Batch #'
                },
                {
                    id: 'virtualCompoundId', name: 'Virtual Compound Id'
                },
                {
                    id: 'stereoisomer', name: 'Stereoisomer',
                    type: 'select',
                    values: function () {
                        return stereoisomerValues;
                    },
                    width: '350px'
                },
                {
                    id: 'source', name: 'Source',
                    type: 'select',
                    values: function () {
                        return sourceValues;
                    },
                    onChange: function (row) {
                        row.sourceDetail = {};
                    },
                    hideSelectValue: true,
                    actions: [_.extend({}, setSelectSourceValueAction, {
                        name: 'Set value for Source',
                        title: 'Source'
                    })]
                },
                {
                    id: 'sourceDetail', name: 'Source Detail',
                    type: 'select',
                    values: function (row) {
                        if (row.source && row.source.name) {
                            if (row.source.name === 'Internal') {
                                return sourceDetailInternal;
                            } else if (row.source.name === 'External') {
                                return sourceDetailExternal;
                            }

                        }
                        return null;
                    },
                    hideSelectValue: true,
                    actions: [_.extend({}, setSelectSourceValueAction, {
                        name: 'Set value for Source Detail',
                        title: 'Source Detail'
                    })]
                },
                {
                    id: '$$externalSupplier',
                    realId: 'externalSupplier',
                    name: 'Ext Supplier',
                    type: 'popup',
                    openPopup: function (data) {
                        InfoEditor.editExternalSupplier(data.row.externalSupplier, function (result) {
                            data.row.externalSupplier = result;
                        });
                    }
                },
                {
                    id: 'precursors',
                    name: 'Precursors',
                    type: 'input'
                },
                {
                    id: '$$healthHazards',
                    realId: 'healthHazards',
                    name: 'Hazards',
                    type: 'popup',
                    openPopup: function (data) {
                        InfoEditor.editHealthHazards(data.row.healthHazards, function (result) {
                            data.row.healthHazards = result;
                        });
                    }

                },
                {
                    id: 'compoundProtection',
                    name: 'Compound Protection',
                    type: 'select',
                    values: function () {
                        return compoundProtectionValues;
                    }
                },
                {
                    id: 'structureComments', name: 'Structure Comments',
                    type: 'input'
                },
                {
                    id: 'registrationDate', name: 'Registration Date', format: function (val) {
                    return $filter('date')(val, 'MMM dd yyyy');
                }
                },
                {id: 'registrationStatus', name: 'Registration Status'}
            ];

            $scope.onRowSelected = function (row) {
                $scope.share.selectedRow = row || null;
                if (row) {
                    $rootScope.$broadcast('batch-summary-row-selected', row);
                } else {
                    $rootScope.$broadcast('batch-summary-row-deselected');
                }
            };

            $scope.share.selectedRow = _.findWhere($scope.model.productBatchSummary.batches, {$$selected: true});

            $scope.isEditable = function (row, columnId) {
                var rowResult = !(row.registrationStatus === 'PASSED' || row.registrationStatus === 'IN_PROGRESS');
                if (rowResult) {
                    if (columnId === 'precursors') {
                        if ($scope.share.stoichTable) {
                            return false;
                        }
                    }
                }
                return rowResult;
            };

            var recalculateSalt = function (reagent) {
                function callback(result) {
                    var data = result.data;
                    reagent.molWeight = reagent.molWeight || {};
                    reagent.molWeight.value = data.molecularWeight;
                    reagent.formula = CalculationService.getSaltFormula(data);
                }

                CalculationService.recalculateSalt(reagent, callback);
            };

            function updatePrecursor() {
                if (!$scope.share.stoichTable) {
                    return;
                }
                _.findWhere($scope.columns, {id: 'precursors'}).readonly = true;
                var precursors = _.filter(_.map($scope.share.stoichTable.reactants, function (item) {
                    return item.compoundId || item.fullNbkBatch;
                }), function (val) {
                    return !!val;
                }).join(', ');
                _.each($scope.model.productBatchSummary.batches, function (batch) {
                    batch.precursors = precursors;
                });
            }

            unbinds.push($scope.$watch('share.stoichTable', updatePrecursor, true));
            unbinds.push($scope.$watch('model.productBatchSummary.batches', updatePrecursor, true));

            $scope.isHasCheckedRows = function () {
                return !!_.find($scope.model.productBatchSummary.batches, function (item) {
                    return item.select;
                });
            };
            var getSelectedNonEditableBatches = function () {
                return _.chain($scope.model.productBatchSummary.batches).filter(function (item) {
                    return item.select;
                }).filter(function (item) {
                    return item.registrationStatus === 'PASSED' || item.registrationStatus === 'IN_PROGRESS';
                }).map(function (item) {
                    return item.fullNbkBatch;
                }).value();
            };
            $scope.deleteBatches = function () {
                var nonEditableBatches = getSelectedNonEditableBatches();
                if (nonEditableBatches && nonEditableBatches.length > 0) {
                    AlertModal.error('Following batches were registered or sent to registration and cannot be deleted: ' + _.uniq(nonEditableBatches).join(', '));
                    return;
                }
                $scope.model.productBatchSummary.batches = _.filter($scope.model.productBatchSummary.batches, function (item) {
                    return !item.select;
                });
            };
            unbinds.push($scope.$watch('showStructures', function (showStructures) {
                var structureColumn = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });
                structureColumn.isVisible = showStructures;
            }));

            unbinds.push($scope.$watch('structureSize', function (newVal) {
                var column = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });
                column.width = 300 * newVal + 'px';
            }));

            unbinds.push($scope.$watch('isHasRegService', function (val) {
                _.findWhere($scope.columns, {id: 'conversationalBatchNumber'}).isVisible = val;
                _.findWhere($scope.columns, {id: 'registrationDate'}).isVisible = val;
                _.findWhere($scope.columns, {id: 'registrationStatus'}).isVisible = val;
            }));
            RegistrationService.info({}, function (info) {
                $scope.isHasRegService = _.isArray(info) && info.length > 0;
            });

            var registerBatches = function (excludes) {
                var batches = _.filter($scope.model.productBatchSummary.batches, function (row) {
                    return row.select && !_.contains(excludes, row.fullNbkBatch);
                });
                var emptyFields = [];
                _.each($scope.columns, function (column) {
                    if (column.type && !column.readonly && column.name !== 'Select') {
                        _.each(batches, function (row) {
                            var val = row[column.id];
                            if (!val) {
                                emptyFields.push(column.name);
                            }
                        });
                    }
                });
                if (emptyFields.length) {
                    AlertModal.error('This fields are required: ' + _.uniq(emptyFields).join(', '));
                } else {
                    var batchNumbers = _.map(batches, function (batch) {
                        return batch.fullNbkBatch;
                    });
                    RegistrationService.register({}, batchNumbers);
                }
            };
            $scope.registerBatches = function () {
                var nonEditableBatches = getSelectedNonEditableBatches();
                if (nonEditableBatches && nonEditableBatches.length > 0) {
                    AlertModal.info('Batch(es) ' + _.uniq(nonEditableBatches).join(', ') + ' already have been registered.', null, function () {
                        registerBatches(nonEditableBatches);
                    });
                } else {
                    registerBatches([]);
                }

            };
            $rootScope.$on('batch-registration-status-changed', function (event, statuses) {
                _.each(statuses, function (status, fullNbkBatch) {
                    var batch = _.find($scope.model.productBatchSummary.batches, function (batch) {
                        return batch.fullNbkBatch === fullNbkBatch;
                    });
                    if (batch) {
                        batch.registrationStatus = status.status;
                        if (status.compoundNumbers) {
                            batch.compoundId = status.compoundNumbers[fullNbkBatch];
                        }
                        if (status.conversationalBatchNumbers) {
                            batch.conversationalBatchNumber = status.conversationalBatchNumbers[fullNbkBatch];
                        }
                    }
                });
            });

            function getLatestNbkBatch() {
                var batches = $scope.model.productBatchSummary.batches;
                return batches && batches.length > 0 && batches[batches.length - 1].nbkBatch ? batches[batches.length - 1].nbkBatch : 0;
            }

            function requestNbkBatchNumberAndAddToTable(duplicatedBatch) {
                var latest = getLatestNbkBatch();
                var deferred = $q.defer();
                $http.get('api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                    '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest)
                    .then(function (result) {
                        var batchNumber = result.data.batchNumber;
                        EntitiesBrowser.resolveFromCache({
                            projectId: $stateParams.projectId,
                            notebookId: $stateParams.notebookId
                        }).then(function (notebook) {
                            var fullNbkBatch = notebook.name + '-' + $scope.experiment.name + '-' + batchNumber;
                            var fullNbkImmutablePart = notebook.name + '-' + $scope.experiment.name + '-';
                            _.each($scope.model.productBatchSummary.batches, function (row) {
                                row.$$selected = false;
                            });
                            var batch = {};
                            if (stoichTable) {
                                batch = angular.copy(CalculationService.createBatch(stoichTable, true));
                            }
                            batch.nbkBatch = batchNumber;
                            batch.fullNbkBatch = fullNbkBatch;
                            batch.fullNbkImmutablePart = fullNbkImmutablePart;
                            batch.$$selected = true;
                            if (duplicatedBatch) {
                                duplicatedBatch.fullNbkBatch = batch.fullNbkBatch;
                                duplicatedBatch.fullNbkImmutablePart = batch.fullNbkImmutablePart;
                                duplicatedBatch.nbkBatch = batch.nbkBatch;
                                duplicatedBatch.conversationalBatchNumber = null;
                                duplicatedBatch.registrationDate = null;
                                duplicatedBatch.registrationStatus = null;
                                batch = duplicatedBatch;
                            }
                            $scope.model.productBatchSummary.batches.push(batch);
                            $scope.onRowSelected(batch);
                            deferred.resolve();
                        });

                    });
                return deferred.promise;
            }

            $scope.addNewBatch = function () {
                requestNbkBatchNumberAndAddToTable();
            };

            $scope.duplicateBatches = function (batchesQueueToAdd, i) {
                if (!batchesQueueToAdd[i]) {
                    return;
                }
                var batchToCopy = batchesQueueToAdd[i];
                var batchToDuplicate = angular.copy(batchToCopy);
                requestNbkBatchNumberAndAddToTable(batchToDuplicate).then(function () {
                    $scope.duplicateBatches(batchesQueueToAdd, i + 1);
                });
            };

            $scope.duplicateBatch = function () {
                var batchToDuplicate = angular.copy($scope.share.selectedRow);
                return requestNbkBatchNumberAndAddToTable(batchToDuplicate);
            };

            $scope.syncWithIntendedProducts = function () {
                var syncingIntendedProducts = $q.defer();
                var batchesQueueToAdd = [];
                $scope.syncingIntendedProducts = syncingIntendedProducts.promise;
                if (stoichTable && stoichTable.products && stoichTable.products.length) {
                    _.each(stoichTable.products, function (intendedItem) {
                        var isUnique = _.every($scope.model.productBatchSummary.batches, function (productItem) {
                            return !angular.equals(intendedItem, productItem);
                        });
                        if (isUnique) {
                            batchesQueueToAdd.push(intendedItem);
                        }
                    });
                    if (!batchesQueueToAdd.length) {
                        syncingIntendedProducts.resolve();
                        AlertModal.info('Product Batch Summary is synchronized', 'sm');
                    } else {
                        $scope.duplicateBatches(batchesQueueToAdd, 0);
                    }
                }
                syncingIntendedProducts.resolve();
            };

            var onProductBatchStructureChanged = $scope.$on('product-batch-structure-changed', function (event, row) {
                var resetMolInfo = function () {
                    row.formula = null;
                    row.molWeight = null;
                };
                var getInfoCallback = function (molInfo) {
                    row.formula = molInfo.data.molecularFormula;
                    row.molWeight = row.molWeight || {};
                    row.molWeight.value = molInfo.data.molecularWeight;
                    CalculationService.calculateProductBatch({row: row, column: ''});
                };
                if (row.structure && row.structure.molfile) {
                    CalculationService.getMoleculeInfo(row, getInfoCallback, resetMolInfo);
                } else {
                    resetMolInfo();
                }
            });

            var onProductBatchSummaryRecalculated = $scope.$on('product-batch-summary-recalculated', function (event, data) {
                if (data.length === $scope.model.productBatchSummary.batches.length) {
                    _.each($scope.model.productBatchSummary.batches, function (batch, i) {
                        _.extend(batch, data[i]);
                    });
                }
            });
            $scope.$on('$destroy', function () {
                onProductBatchSummaryRecalculated();
                onProductBatchStructureChanged();
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
            $scope.$on('$destroy', function () {
                _.each(unbinds, function (unbind) {
                    unbind();
                });
            });
        }
    );