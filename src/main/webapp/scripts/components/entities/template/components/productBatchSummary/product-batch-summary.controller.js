/**
 * Created by Stepan_Litvinov on 3/2/2016.
 */
'use strict';

angular.module('indigoeln')
    .controller('ProductBatchSummaryController',
        function ($scope, $uibModal, $http, $stateParams) {
            $scope.model = $scope.model || {};
            $scope.model.productBatchSummary = $scope.model.productBatchSummary || {};
            $scope.model.productBatchSummary.batches = $scope.model.productBatchSummary.batches || [];

            var convertUnit = function (name, item) {
                if (!item) {
                    return;
                }
                item.value = $u(item.value, item.unit).as(name).val();
                item.unit = name;
            };
            var grams = ['mg', 'g', 'kg'];
            var liters = ['ul', 'ml', 'l'];
            var moles = ['umol', 'mmol', 'mol'];
            var toUnitNameAction = function (unit) {
                return {
                    name: unit,
                    onClick: convertUnit.bind(null, unit)
                };
            };
            var toUnitAction = function (unit) {
                return {
                    name: 'Set Unit ' + unit,
                    action: function (id) {
                        _.each($scope.model.productBatchSummary.batches, function (row) {
                            convertUnit(unit, row[id]);
                        });
                    }
                };
            };

            var gramsUnits = _.map(grams, toUnitNameAction);
            var gramsActions = _.map(grams, toUnitAction);
            var litersUnits = _.map(liters, toUnitNameAction);
            var litersActions = _.map(liters, toUnitAction);
            var molesUnits = _.map(moles, toUnitNameAction);
            var molesActions = _.map(moles, toUnitAction);

            var setUnitValueAction = {
                action: function (id) {
                    var that = this;
                    $uibModal.open({
                        templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary-set-unit-value.html',
                        controller: 'ProductBatchSummarySetUnitValueController',
                        size: 'sm',
                        resolve: {
                            name: function () {
                                return that.title;
                            },
                            unitNames: function () {
                                return that.units;
                            }
                        }
                    }).result.then(function (result) {
                        _.each($scope.model.productBatchSummary.batches, function (item) {
                            item[id] = item[id] || {};
                            item[id].value = result.value;
                            item[id].unit = result.unit;
                        });
                    }, function () {

                    });
                }

            };
            var setSelectValueAction = {
                action: function (id) {
                    var that = this;
                    $uibModal.open({
                        templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary-set-select-value.html',
                        controller: 'ProductBatchSummarySetSelectValueController',
                        size: 'sm',
                        resolve: {
                            name: function () {
                                return that.title;
                            },
                            values: function () {
                                return that.values;
                            }
                        }
                    }).result.then(function (result) {
                        _.each($scope.model.productBatchSummary.batches, function (item) {
                            item[id] = item[id] || {};
                            item[id].name = result.name;
                        });
                    }, function () {

                    });
                }

            };

            var compoundValues = [{name: 'Solid'}, {name: 'Glass'}, {name: 'Gum'}, {name: 'Mix'}, {name: 'Liquid/Oil'}, {name: 'Solution'}];
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
                    units: gramsUnits,
                    actions: gramsActions,
                    readonly: true
                },
                {
                    id: 'totalWeight',
                    name: 'Total Weight',
                    type: 'unit',
                    width: '150px',
                    units: gramsUnits,
                    actions: [_.extend({}, setUnitValueAction, {
                        name: 'Set value for Total Weight',
                        title: 'Total Weight',
                        units: grams
                    })].concat(gramsActions)
                },
                {
                    id: 'totalVolume', name: 'Total Volume',
                    type: 'unit',
                    width: '150px',
                    units: litersUnits,
                    actions: [_.extend({}, setUnitValueAction, {
                        name: 'Set value for Total Volume',
                        title: 'Total Volume',
                        units: liters
                    })].concat(litersActions)
                },
                {
                    id: 'totalMoles', name: 'Total Moles',
                    type: 'unit',
                    width: '150px',
                    units: molesUnits,
                    actions: [_.extend({}, setUnitValueAction, {
                        name: 'Set value for Total Moles',
                        title: 'Total Moles',
                        units: moles
                    })].concat(molesActions)
                },
                {
                    id: 'theoMoles', name: 'Theo. Moles',
                    type: 'unit',
                    width: '150px',
                    units: gramsUnits,
                    actions: [_.extend({}, setUnitValueAction, {
                        name: 'Set value for Theo. Moles',
                        title: 'Theo. Moles',
                        units: grams
                    })].concat(gramsActions)
                },
                {id: 'yield', name: '%Yield'},
                {
                    id: 'compoundState',
                    name: 'Compound State',
                    type: 'select',
                    values: function () {
                        return compoundValues;
                    },
                    actions: [_.extend({}, setSelectValueAction, {
                        name: 'Set value for Compound State',
                        title: 'Compound State',
                        values: compoundValues
                    })]

                },
                {id: 'purity', name: 'Purity'},
                {id: 'meltingPoint', name: 'Melting Point'},
                {id: 'molWgt', name: 'Mol Wgt'},
                {id: 'molFormula', name: 'Mol Formula'},
                {id: 'conversationalBatch', name: 'Conversational Batch #'},
                {id: 'virtualCompoundId', name: 'Virtual Compound Id'},
                {
                    id: 'stereoisomer', name: 'Stereoisomer',
                    type: 'select',
                    values: function () {
                        return stereoisomerValues;
                    },
                    width: '350px',
                    actions: [_.extend({}, setSelectValueAction, {
                        name: 'Set value for Stereoisomer',
                        title: 'Stereoisomer',
                        values: stereoisomerValues
                    })]
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
                    },
                    actions: [_.extend({}, setSelectValueAction, {
                        name: 'Set value for Compound Protection',
                        title: 'Compound Protection',
                        values: compoundProtectionValues
                    })]
                },
                {
                    id: 'structureComments', name: 'Structure Comments',
                    type: 'input'
                }
            ];

            $scope.onRowSelected = function (row) {
                $scope.share.selectedRow = row;
            };
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

            $scope.addNewBatch = function () {
                var batches = $scope.model.productBatchSummary.batches;
                var latest = batches && batches.length > 0 && batches[batches.length - 1].nbkBatch ? batches[batches.length - 1].nbkBatch : 0;

                $http.get('api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                        '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest)
                    .then(function (result) {
                        $scope.model.productBatchSummary.batches.push({nbkBatch: result.data.batchNumber});
                    });
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
                            row.molWgt = null;
                        };
                        if (newMolFile) {
                            $http.put('api/calculations/molecule/info', row.structure.molfile)
                                .then(function (molInfo) {
                                    row.molFormula = molInfo.data.molecularFormula;
                                    row.molWgt = molInfo.data.molecularWeight;
                                }, resetMolInfo);
                        } else {
                            resetMolInfo();
                        }
                    }));
                });
            }, true);
        }
    );