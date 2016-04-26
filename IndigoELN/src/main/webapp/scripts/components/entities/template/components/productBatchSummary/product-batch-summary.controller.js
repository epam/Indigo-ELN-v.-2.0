/**
 * Created by Stepan_Litvinov on 3/2/2016.
 */
'use strict';

angular.module('indigoeln')
    .controller('ProductBatchSummaryController',
        function ($scope, $rootScope, $uibModal, $http, $stateParams, EntitiesBrowser, AlertModal) {
            $scope.model = $scope.model || {};
            $scope.model.productBatchSummary = $scope.model.productBatchSummary || {};
            $scope.model.productBatchSummary.batches = $scope.model.productBatchSummary.batches || [];
            var grams = ['mg', 'g', 'kg'];
            var liters = ['ul', 'ml', 'l'];
            var moles = ['umol', 'mmol', 'mol'];
            var compoundValues = [{name: 'Solid'}, {name: 'Glass'}, {name: 'Gum'}, {name: 'Mix'}, {name: 'Liquid/Oil'}, {name: 'Solution'}];
            var saltCodeValues = [
                {name: '00 - Parent Structure', value: '0'},
                {name: '01 - HYDROCHLORIDE', value: '1'},
                {name: '02 - SODIUM', value: '2'},
                {name: '03 - HYDRATE', value: '3'},
                {name: '04 - HYDROBROMIDE', value: '4'},
                {name: '05 - HYDROIODIDE', value: '5'},
                {name: '06 - POTASSIUM', value: '6'},
                {name: '07 - CALCIUM', value: '7'},
                {name: '08 - SULFATE', value: '8'},
                {name: '09 - PHOSPHATE', value: '9'},
                {name: '10 - CITRATE', value: '10'}
            ];
            var stereoisomerValues = [
                {name: 'NOSTC - Achiral - No Stereo Centers'},
                {name: 'AMESO - Achiral - Meso Stereomers'},
                {name: 'CISTR - Achiral - Cis/Trans Stereomers'},
                {name: 'SNENK - Single Enantiomer (chirality known)'},
                {name: 'RMCMX - Racemic (stereochemistry known)'},
                {name: 'ENENK - Enantio-Enriched (chirality known)'},
                {name: 'DSTRK - Diastereomers (stereochemistry known)'},
                {name: 'SNENU - Other - Single Enantiomer (chirality unknown)'}];
            var sourceValues = [
                {name: 'Internal'},
                {name: 'External'}];
            var sourceDetailExternal = [{name: 'External group 1'}, {name: 'External group 2'}, {name: 'External group 3'}];
            var sourceDetailInternal = [{name: 'Internal group 1'}, {name: 'Internal group 2'}, {name: 'Internal group 3'}];
            var compoundProtectionValues = [
                {name: 'NONE - None'},
                {name: 'ST1 - Standard 1'},
                {name: 'ST2 - Standard 2'}];
            var setSelectSourceValueAction = {
                action: function (id) {
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

            $scope.columns = [
                {
                    id: 'structure',
                    name: 'Structure',
                    type: 'image',
                    isHidden: true,
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
                    id: 'theoWgt',
                    name: 'Theo. Wgt.',
                    type: 'unit',
                    width: '150px',
                    unitItems: grams,
                    hideSetValue: true,
                    readonly: true
                },
                {
                    id: 'totalWeight',
                    name: 'Total Weight',
                    type: 'unit',
                    width: '150px',
                    unitItems: grams
                },
                {
                    id: 'totalVolume', name: 'Total Volume',
                    type: 'unit',
                    width: '150px',
                    unitItems: liters
                },
                {
                    id: 'totalMoles', name: 'Total Moles',
                    type: 'unit',
                    width: '150px',
                    unitItems: moles
                },
                {
                    id: 'theoMoles', name: 'Theo. Moles',
                    type: 'unit',
                    width: '150px',
                    unitItems: grams
                },
                {id: 'yield', name: '%Yield'},
                {
                    id: 'saltCode',
                    name: 'Salt Code & Name',
                    type: 'select',
                    values: function () {
                        return saltCodeValues;
                    }
                },
                {id: 'saltEq', name: 'Salt Equivalent', type: 'input'},
                {
                    id: 'compoundState',
                    name: 'Compound State',
                    type: 'select',
                    values: function () {
                        return compoundValues;
                    }

                },
                {id: 'purity', name: 'Purity'},
                {id: 'meltingPoint', name: 'Melting Point'},
                {id: 'molWt', name: 'Mol Wgt'},
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
                    onSelect: function (row) {
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
                {id: 'extSupplier', name: 'Ext Supplier'},
                {
                    id: 'precursors', name: 'Precursors',
                    type: 'input'
                },
                {id: 'hazards', name: 'Hazards'},
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
                //if (structureColumn) {
                structureColumn.isHidden = !showStructures;
                //}
            });

            $scope.$watch('structureSize', function (newVal) {
                var column = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });
                column.width = 300 * newVal + 'px';
            });

            $scope.$watch('isHasRegService', function (val) {
                _.findWhere($scope.columns, {id: 'conversationalBatch'}).isHidden = !val;
                _.findWhere($scope.columns, {id: 'regDate'}).isHidden = !val;
                _.findWhere($scope.columns, {id: 'regStatus'}).isHidden = !val;
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
                            var batch = {
                                nbkBatch: batchNumber,
                                fullNbkBatch: fullNbkBatch,
                                fullNbkImmutablePart: fullNbkImmutablePart,
                                $$selected: false
                            };
                            if(batchToDuplicate) {
                                batch = _.extend(batchToDuplicate, batch);
                            }
                            $scope.model.productBatchSummary.batches.push(batch);
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
                            row.molWt = null;
                        };
                        if (newMolFile) {
                            var config = {params: {
                                saltCode: row.saltCode ? row.saltCode.value : null,
                                saltEq: row.saltEq}};
                            $http.put('api/calculations/molecule/info', row.structure.molfile, config)
                                .then(function (molInfo) {
                                    row.molFormula = molInfo.data.molecularFormula;
                                    row.molWt = molInfo.data.molecularWeight;
                                }, resetMolInfo);
                        } else {
                            resetMolInfo();
                        }
                    }));
                });
            }, true);
        }
    );