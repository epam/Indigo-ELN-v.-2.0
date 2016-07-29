/**
 * Created by Stepan_Litvinov on 3/2/2016.
 */
angular.module('indigoeln')
    .controller('ProductBatchSummaryController',
        function ($scope, $rootScope, $uibModal, $http, $stateParams, $q, $filter, $log, InfoEditor, EntitiesBrowser, AlertModal, AppValues, CalculationService, RegistrationService) {
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

            function getStoicTable() {
                return stoichTable;
            }

            function setStoicTable(table) {
                stoichTable = table;
            }
            var getProductBatches = function () {
                return $scope.model.productBatchSummary.batches;
            };
            var setProductBatches = function (batches) {
                $scope.model.productBatchSummary.batches = batches;
            };
            var addProductBatch = function (batch) {
                $scope.model.productBatchSummary.batches.push(batch);
            };

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
                        _.each(getProductBatches(), function (row) {
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

            function updatePrecursor() {
                if (!getStoicTable()) {
                    return;
                }
                _.findWhere($scope.columns, {id: 'precursors'}).readonly = true;
                var precursors = _.filter(_.map($scope.share.stoichTable.reactants, function (item) {
                    return item.compoundId || item.fullNbkBatch;
                }), function (val) {
                    return !!val;
                }).join(', ');
                _.each(getProductBatches(), function (batch) {
                    batch.precursors = precursors;
                });
            }

            unbinds.push($scope.$watch('model.productBatchSummary.batches', updatePrecursor, true));

            unbinds.push($scope.$watch('share.stoichTable', function (table) {
                setStoicTable(table);
                updatePrecursor();
            }, true));

            function editMeltingPoint(row) {
                InfoEditor.editMeltingPoint(row.meltingPoint, function (result) {
                    row.meltingPoint = result;
                });
            }

            function editMeltingPointForAllRows(rows) {
                InfoEditor.editMeltingPoint({}, function (result) {
                    _.each(rows, function (row) {
                        row.meltingPoint = angular.copy(result);
                    });
                });
            }

            function editPurity(row) {
                InfoEditor.editPurity(row.purity, function (result) {
                    row.purity = result;
                });
            }

            function editPurityForAllRows(rows) {
                InfoEditor.editPurity({}, function (result) {
                    _.each(rows, function (row) {
                        row.purity = angular.copy(result);
                    });
                });
            }

            function editExternalSupplier(row) {
                InfoEditor.editExternalSupplier(row.externalSupplier, function (result) {
                    row.externalSupplier = result;
                });
            }

            function editExternalSupplierForAllRows(rows) {
                InfoEditor.editExternalSupplier({}, function (result) {
                    _.each(rows, function (row) {
                        row.externalSupplier = angular.copy(result);
                    });
                });
            }

            function editHealthHazards(row) {
                InfoEditor.editHealthHazards(row.healthHazards, function (result) {
                    row.healthHazards = result;
                });
            }

            function editHealthHazardsForAllRows(rows) {
                InfoEditor.editHealthHazards({}, function (result) {
                    _.each(rows, function (row) {
                        row.healthHazards = angular.copy(result);
                    });
                });
            }

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
                                _.each(getProductBatches(), function (row) {
                                    row.select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function () {
                                _.each(getProductBatches(), function (row) {
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
                    type: 'string',
                    onClick: function (data) {
                        editPurity(data.row);
                    },
                    actions: [{
                        name: 'Set value for Purity',
                        title: 'Purity',
                        rows: getProductBatches(),
                        action: function () {
                            editPurityForAllRows(getProductBatches());
                        }
                    }]
                },
                {
                    id: '$$meltingPoint',
                    realId: 'meltingPoint',
                    name: 'Melting Point',
                    type: 'string',
                    onClick: function (data) {
                        editMeltingPoint(data.row);
                    },
                    actions: [{
                        name: 'Set value for Melting Point',
                        title: 'Melting Point',
                        rows: getProductBatches(),
                        action: function () {
                            editMeltingPointForAllRows(getProductBatches());
                        }
                    }]
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
                    type: 'string',
                    onClick: function (data) {
                        editExternalSupplier(data.row);
                    },
                    actions: [{
                        name: 'Set value for External Supplier',
                        title: 'External Supplier',
                        rows: getProductBatches(),
                        action: function () {
                            editExternalSupplierForAllRows(getProductBatches());
                        }
                    }]
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
                    type: 'string',
                    onClick: function (data) {
                        editHealthHazards(data.row);
                    },
                    actions: [{
                        name: 'Set value for Health Hazards',
                        title: 'Health Hazards',
                        rows: getProductBatches(),
                        action: function () {
                            editHealthHazardsForAllRows(getProductBatches());
                        }
                    }]

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
                    return val ? $filter('date')(val, 'MMM DD, YYYY HH:mm:ss z') : null;
                }
                },
                {id: 'registrationStatus', name: 'Registration Status'}
            ];

            $scope.onRowSelected = function (row) {
                $scope.share.selectedRow = row || null;
                if (row) {
                    var data = initDataForCalculation();
                    data.row = row;
                    $rootScope.$broadcast('batch-summary-row-selected', data);
                } else {
                    $rootScope.$broadcast('batch-summary-row-deselected');
                }
                $log.debug(row);
            };

            $scope.share.selectedRow = _.findWhere(getProductBatches(), {$$selected: true});

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

            function initDataForCalculation(data) {
                var calcData = data || {};
                calcData.stoichTable = getStoicTable();
                calcData.actualProducts = getProductBatches();
                return calcData;
            }

            var recalculateSalt = function (reagent) {
                function callback(result) {
                    var data = result.data;
                    data.mySaltEq = reagent.saltEq;
                    data.mySaltCode = reagent.saltCode;
                    reagent.molWeight = reagent.molWeight || {};
                    reagent.molWeight.value = data.molecularWeight;
                    reagent.formula = CalculationService.getSaltFormula(data);
                    reagent.lastUpdatedType = 'weight'; // is it?
                    CalculationService.recalculateAmounts({row: reagent});
                }
                CalculationService.recalculateSalt(reagent, callback);
            };

            $scope.isHasCheckedRows = function () {
                return !!_.find(getProductBatches(), function (item) {
                    return item.select;
                });
            };
            var getSelectedNonEditableBatches = function () {
                return _.chain(getProductBatches()).filter(function (item) {
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
                setProductBatches(_.filter(getProductBatches(), function (item) {
                    return !item.select;
                }));
            };
            $scope.showStructuresColumn = _.find($scope.columns, function (item) {
                return item.id === 'structure';
            });
            $scope.toggleShowStructures = function () {
                $scope.showStructuresColumn.isVisible = !$scope.showStructuresColumn.isVisible;
            };
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
                var batches = _.filter(getProductBatches(), function (row) {
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
                    var batch = _.find(getProductBatches(), function (batch) {
                        return batch.fullNbkBatch === fullNbkBatch;
                    });
                    if (batch) {
                        batch.registrationStatus = status.status;
                        batch.registrationDate = status.date;
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
                var batches = getProductBatches();
                return batches && batches.length > 0 && batches[batches.length - 1].nbkBatch ? batches[batches.length - 1].nbkBatch : 0;
            }

            function requestNbkBatchNumberAndAddToTable(duplicatedBatch, isSyncWithIntended) {
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
                            _.each(getProductBatches(), function (row) {
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
                                if (isSyncWithIntended) {
                                    // to sync mapping of intended products with actual poducts
                                    duplicatedBatch.theoMoles = duplicatedBatch.mol;
                                    duplicatedBatch.theoWeight = duplicatedBatch.weight;
                                    // total moles can be calculated when total weight or total Volume are added, or manually
                                    duplicatedBatch.mol = null;
                                }
                                batch = duplicatedBatch;
                            }
                            addProductBatch(batch);
                            $log.debug(batch);
                            $scope.onRowSelected(batch);
                            deferred.resolve();
                        });

                    });
                return deferred.promise;
            }

            $scope.addNewBatch = function () {
                requestNbkBatchNumberAndAddToTable();
            };

            $scope.duplicateBatches = function (batchesQueueToAdd, i, isSyncWithIntended) {
                if (!batchesQueueToAdd[i]) {
                    return;
                }
                var batchToCopy = batchesQueueToAdd[i];
                var batchToDuplicate = angular.copy(batchToCopy);
                requestNbkBatchNumberAndAddToTable(batchToDuplicate, isSyncWithIntended).then(function () {
                    $scope.duplicateBatches(batchesQueueToAdd, i + 1, isSyncWithIntended);
                });
            };

            $scope.duplicateBatch = function () {
                var productBatches = getProductBatches();
                var batchesToDuplicate = _.filter(productBatches, function (item) {
                    return item.select;
                });
                $scope.duplicateBatches(batchesToDuplicate, 0);
            };

            function removeItemFromBothArrays(item, array1, array2, i) {
                if (_.contains(array1, item)) {
                    array2[i] = null;
                    array1[_.indexOf(array1, item)] = null;
                }
            }

            var getIntendedNotInActual = function () {
                if (stoichTable) {
                    var intended = stoichTable.products;
                    var intendedCandidateHashes = _.pluck(intended, '$$batchHash');
                    var actual = getProductBatches();
                    var actualHashes = _.compact(_.pluck(actual, '$$batchHash'));
                    _.each(intendedCandidateHashes, function (intendedCandidateHash, i) {
                        removeItemFromBothArrays(intendedCandidateHash, actualHashes, intendedCandidateHashes, i);
                    });
                    var hashesToAdd = _.compact(intendedCandidateHashes);
                    return _.map(hashesToAdd, function (hash) {
                        return _.findWhere(intended, {$$batchHash: hash});
                    });
                }
            };

            $scope.isIntendedSynced = function () {
                return getIntendedNotInActual() ? !getIntendedNotInActual().length : true;
            };

            $scope.syncWithIntendedProducts = function () {
                var syncingIntendedProducts = $q.defer();
                var batchesQueueToAdd = getIntendedNotInActual();

                $scope.syncingIntendedProducts = syncingIntendedProducts.promise;
                if (stoichTable && stoichTable.products && stoichTable.products.length) {
                    if (!batchesQueueToAdd.length) {
                        syncingIntendedProducts.resolve();
                        AlertModal.info('Product Batch Summary is synchronized', 'sm');
                    } else {
                        $scope.duplicateBatches(batchesQueueToAdd, 0, true);
                    }
                }
                syncingIntendedProducts.resolve();
            };

            $scope.importSDFile = function () {
                $uibModal.open({
                    animation: true,
                    size: 'lg',
                    templateUrl: 'scripts/components/fileuploader/single-file-uploader/single-file-uploader-modal.html',
                    controller: 'SingleFileUploaderController'
                }).result.then(function (result) {
                    $log.debug(result);
                });
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
                    // todo replace with recalcAmounts?
                    CalculationService.recalculateStoich(initDataForCalculation());
                };
                if (row.structure && row.structure.molfile) {
                    CalculationService.getMoleculeInfo(row, getInfoCallback, resetMolInfo);
                } else {
                    resetMolInfo();
                }
            });

            var onProductBatchSummaryRecalculated = $scope.$on('product-batch-summary-recalculated', function (event, data) {
                if (data.length === getProductBatches().length) {
                    _.each(getProductBatches(), function (batch, i) {
                        _.extend(batch, data[i]);
                    });
                }
            });
            $scope.$on('$destroy', function () {
                onProductBatchSummaryRecalculated();
                onProductBatchStructureChanged();
            });
            $scope.$on('$destroy', function () {
                _.each(unbinds, function (unbind) {
                    unbind();
                });
            });
        }
    );