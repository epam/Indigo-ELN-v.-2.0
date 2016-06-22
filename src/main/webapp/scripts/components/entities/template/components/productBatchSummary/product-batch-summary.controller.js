/**
 * Created by Stepan_Litvinov on 3/2/2016.
 */
angular.module('indigoeln')
    .controller('ProductBatchSummaryController',
        function ($scope, $rootScope, $uibModal, $http, $stateParams, EntitiesBrowser, AlertModal, AppValues, CalculationService) {
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

            $scope.$watch('model.productBatchSummary.batches', function (batches) {
                _.each(batches, function (batch) {
                    batch.$$purity = batch.purity ? batch.purity.asString : null;
                    batch.$$externalSupplier = batch.externalSupplier ? batch.externalSupplier.asString : null;
                    batch.$$meltingPoint = batch.meltingPoint ? batch.meltingPoint.asString : null;
                    batch.$$healthHazards = batch.healthHazards ? batch.healthHazards.asString : null;
                });
                $scope.share.actualProducts = batches;
            }, true);

            $scope.$watch('share.stoichTable', function (table) {
                stoichTable = table;
            }, true);

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
                    unitItems: grams
                },
                {
                    id: 'totalVolume',
                    name: 'Total Volume',
                    type: 'unit',
                    width: '150px',
                    unitItems: liters
                },
                {
                    id: 'totalMoles',
                    name: 'Total Moles',
                    type: 'unit',
                    width: '150px',
                    unitItems: moles
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
                {id: 'yield', name: '%Yield'},
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
                    name: 'Salt Code & Name',
                    type: 'select',
                    values: function () {
                        return saltCodeValues;
                    }
                },
                {id: 'saltEq', name: 'Salt Equivalent', type: 'scalar'},
                {id: '$$purity', name: 'Purity'},
                {id: '$$meltingPoint', name: 'Melting Point'},
                {id: 'molWeight', name: 'Mol Wgt', type: 'scalar'},
                {id: 'molFormula', name: 'Mol Formula'},
                {id: 'conversationalBatch', name: 'Conversational Batch #'},
                {id: 'virtualCompoundId', name: 'Virtual Compound Id'},
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
                {id: '$$externalSupplier', name: 'Ext Supplier'},
                {
                    id: 'precursors', name: 'Precursors',
                    type: 'input'
                },
                {id: '$$healthHazards', name: 'Hazards'},
                {
                    id: 'compoundProtection', name: 'Compound Protection',
                    type: 'select',
                    values: function () {
                        return compoundProtectionValues;
                    }
                },
                {
                    id: 'structureComments', name: 'Structure Comments',
                    type: 'input'
                },
                {id: 'regDate', name: 'Registration Date'},
                {id: 'regStatus', name: 'Registration Status'}
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

            $scope.isHasCheckedRows = function () {
                return !!_.find($scope.model.productBatchSummary.batches, function (item) {
                    return item.select;
                });
            };
            $scope.deleteBatches = function () {
                $scope.model.productBatchSummary.batches = _.filter($scope.model.productBatchSummary.batches, function (item) {
                    return !item.select;
                });
            };
            $scope.$watch('showStructures', function (showStructures) {
                var structureColumn = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });
                structureColumn.isVisible = showStructures;
            });

            $scope.$watch('structureSize', function (newVal) {
                var column = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });
                column.width = 300 * newVal + 'px';
            });

            $scope.$watch('isHasRegService', function (val) {
                _.findWhere($scope.columns, {id: 'conversationalBatch'}).isVisible = val;
                _.findWhere($scope.columns, {id: 'regDate'}).isVisible = val;
                _.findWhere($scope.columns, {id: 'regStatus'}).isVisible = val;
            });

            $scope.registerBatches = function () {
                var emptyFields = [];
                _.each($scope.columns, function (column) {
                    if (column.type && !column.readonly && column.name !== 'Select') {
                        _.each($scope.model.productBatchSummary.batches, function (row) {
                            var val = row[column.id];
                            if (!val) {
                                emptyFields.push(column.name);
                            }
                        });
                    }
                });
                if (emptyFields.length) {
                    AlertModal.error('This fields is required: ' + _.uniq(emptyFields).join(', '));
                } else {
                    AlertModal.info('Not implemented yet');
                }

            };

            function requestNbkBatchNumber(latest, batchToDuplicate) {
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
                            var batch = _.extend(CalculationService.createBatch(stoichTable, true),
                                {
                                    nbkBatch: batchNumber, fullNbkBatch: fullNbkBatch,
                                    fullNbkImmutablePart: fullNbkImmutablePart, $$selected: true
                                });
                            if(batchToDuplicate) {
                                batch = _.extend(batchToDuplicate, batch);
                            }
                            $scope.model.productBatchSummary.batches.push(batch);
                            $rootScope.$broadcast('batch-summary-row-selected', batch);
                        });

                    });
            }

            $scope.addNewBatch = function () {
                var batches = $scope.model.productBatchSummary.batches;
                var latest = batches && batches.length > 0 && batches[batches.length - 1].nbkBatch ? batches[batches.length - 1].nbkBatch : 0;

                requestNbkBatchNumber(latest);
            };

            $scope.duplicateBatch = function () {
                var batchToDuplicate = angular.copy($scope.share.selectedRow);
                var batches = $scope.model.productBatchSummary.batches;
                var latest = batches && batches.length > 0 && batches[batches.length - 1].nbkBatch ? batches[batches.length - 1].nbkBatch : 0;
                requestNbkBatchNumber(latest, batchToDuplicate);
            };

            var structureWatchers = [];
            $scope.$watch('model.productBatchSummary.batches', function (newRows) {
                _.each(structureWatchers, function (unsubscribe) {
                    unsubscribe();
                });
                _.each(newRows, function (row) {
                    structureWatchers.push($scope.$watch(function () {
                        return row.structure ? row.structure.molfile : null;
                    }, function (newMolFile) {
                        var resetMolInfo = function () {
                            row.molFormula = null;
                            row.molWeight = null;
                        };
                        var getInfoCallback = function (molInfo) {
                            row.molFormula = molInfo.data.molecularFormula;
                            row.molWeight = row.molWeight || {};
                            row.molWeight.value = molInfo.data.molecularWeight;
                        };
                        if (newMolFile) {
                            CalculationService.getMoleculeInfo(row, getInfoCallback, resetMolInfo);
                        } else {
                            resetMolInfo();
                        }
                    }));
                });
            }, true);

            var onProductBatchSummaryRecalculated = $scope.$on('product-batch-summary-recalculated', function (event, data) {
                if (data.length === $scope.model.productBatchSummary.batches.length) {
                    _.each($scope.model.productBatchSummary.batches, function (batch, i) {
                        _.extend(batch, data[i]);
                    });
                }
            });
            $scope.$on('$destroy', function () {
                onProductBatchSummaryRecalculated();
            });
        }
    );