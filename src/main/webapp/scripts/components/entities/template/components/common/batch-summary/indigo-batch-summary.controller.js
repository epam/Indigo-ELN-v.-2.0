(function() {
    angular
        .module('indigoeln')
        .controller('IndigoBatchSummaryController', IndigoBatchSummaryController);

    /* @ngInject */
    function IndigoBatchSummaryController($scope, CalculationService, AppValues, InfoEditor, RegistrationUtil, $uibModal,
                                          $log, $rootScope, EntitiesBrowser, RegistrationService,
                                          ProductBatchSummaryOperations, $filter, ProductBatchSummaryCache) {
        var vm = this;
        var stoichTable;
        var unbinds = [];
        var grams = AppValues.getGrams();
        var liters = AppValues.getLiters();
        var moles = AppValues.getMoles();
        var saltCodeValues = AppValues.getSaltCodeValues();
        var sourceValues = AppValues.getSourceValues();
        var sourceDetailExternal = AppValues.getSourceDetailExternal();
        var sourceDetailInternal = AppValues.getSourceDetailInternal();
        var compoundProtectionValues = AppValues.getCompoundProtectionValues();
        var compounds = [{
            name: 'Intermediate'
        }, {
            name: 'Test Compound'
        }];
        var setSelectSourceValueAction = {
            action: openProductBatchSummaryModal
        };

        init();

        function init() {
            vm.loading = false;
            vm.model = vm.model || {};
            vm.model.productBatchSummary = vm.model.productBatchSummary || {};
            vm.model.productBatchSummary.batches = vm.model.productBatchSummary.batches || [];
            vm.share.selectedRow = _.findWhere(getProductBatches(), {
                $$selected: true
            });
            vm.columns = getDefaultColumns();

            RegistrationService.info({}, function(info) {
                vm.isHasRegService = _.isArray(info) && info.length > 0;
            });

            vm.showStructuresColumn = _.find(vm.columns, function(item) {
                return item.id === 'structure';
            });

            vm.onRowSelected = onRowSelected;
            vm.syncWithIntendedProducts = syncWithIntendedProducts;
            vm.isEditable = isEditable;
            vm.isIntendedSynced = isIntendedSynced;
            vm.importSDFile = importSDFile;
            vm.deleteBatches = deleteBatches;
            vm.isHasCheckedRows = isHasCheckedRows;
            vm.addNewBatch = addNewBatch;
            vm.duplicateBatch = duplicateBatch;
            vm.exportSDFile = exportSDFile;
            vm.registerBatches = registerBatches;

            bindEvents();
        }

        function getProductBatches() {
            return vm.model.productBatchSummary.batches;
        }

        function recalculateSalt(reagent) {
            CalculationService.recalculateSalt(reagent).then(function() {
                CalculationService.recalculateStoich();
            });
        }

        function openProductBatchSummaryModal() {
            var that = this;
            $uibModal.open({
                templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary-set-source.html',
                controller: 'ProductBatchSummarySetSourceController',
                size: 'sm',
                resolve: {
                    name: function() {
                        return that.title;
                    },
                    sourceValues: function() {
                        return sourceValues;
                    },
                    sourceDetailExternal: function() {
                        return sourceDetailExternal;
                    },
                    sourceDetailInternal: function() {
                        return sourceDetailInternal;
                    }
                }
            }).result.then(function(result) {
                _.each(getProductBatches(), function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.source = result.source;
                        row.sourceDetail = result.sourceDetail;
                    }
                });
            }, function() {

            });
        }

        function getDefaultColumns() {
            return [
                {
                    id: 'structure',
                    name: 'Structure',
                    type: 'image',
                    isVisible: false,
                    width: vm.structureSize
                },
                {
                    id: 'nbkBatch',
                    name: 'Nbk Batch #'
                },
                {
                    id: 'registrationStatus', name: 'Registration Status'
                },
                {
                    id: 'select',
                    name: 'Select',
                    type: 'boolean',
                    noDirty: true,
                    actions: [
                        {
                            name: 'Select All',
                            action: function() {
                                _.each(getProductBatches(), function(row) {
                                    row.select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function() {
                                _.each(getProductBatches(), function(row) {
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
                    onClose: function(data) {
                        CalculationService.setEntered(data);
                        var tw = data.row.totalWeight;
                        tw.value = Math.abs(parseInt(tw.value, 10));
                        CalculationService.calculateProductBatch(data);
                    }
                },
                {
                    id: 'totalVolume',
                    name: 'Total Volume',
                    type: 'unit',
                    width: '150px',
                    unitItems: liters,
                    onClose: function(data) {
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
                    onClose: function(data) {
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
                {
                    id: 'yield', name: '%Yield', type: 'primitive', sigDigits: 2, readonly: true
                },
                {
                    id: 'compoundState',
                    name: 'Compound State',
                    type: 'select',
                    dictionary: 'Compound State',
                    values: function() {
                        return null;
                    }
                },
                {
                    id: 'saltCode',
                    name: 'Salt Code',
                    type: 'select',
                    showDefault: true,
                    values: function() {
                        return saltCodeValues;
                    },
                    onClose: function(data) {
                        CalculationService.setEntered(data);
                        recalculateSalt(data.row);
                        if (data.model.value === 0) {
                            data.row.saltEq.value = 0;
                        }
                    }
                },
                {
                    id: 'saltEq',
                    name: 'Salt EQ',
                    type: 'scalar',
                    bulkAssignment: true,
                    checkEnabled: function(o) {
                        return (o.saltCode && o.saltCode.value > 0);
                    },
                    onClose: function(data) {
                        CalculationService.setEntered(data);
                        recalculateSalt(data.row);
                    }
                },
                {
                    id: '$$purity',
                    realId: 'purity',
                    name: 'Purity',
                    type: 'string',
                    onClick: function(data) {
                        editPurity(data.row);
                    },
                    actions: [{
                        name: 'Set value for Purity',
                        title: 'Purity',
                        rows: getProductBatches(),
                        action: function() {
                            editPurityForAllRows(getProductBatches());
                        }
                    }]
                },
                {
                    id: '$$meltingPoint',
                    realId: 'meltingPoint',
                    name: 'Melting Point',
                    type: 'string',
                    onClick: function(data) {
                        editMeltingPoint(data.row);
                    },
                    actions: [{
                        name: 'Set value for Melting Point',
                        title: 'Melting Point',
                        rows: getProductBatches(),
                        action: function() {
                            editMeltingPointForAllRows(getProductBatches());
                        }
                    }]
                },
                {
                    id: 'molWeight',
                    name: 'Mol Wgt',
                    type: 'scalar'
                },
                {
                    id: 'formula',
                    name: 'Mol Formula',
                    type: 'input',
                    readonly: true
                },
                {
                    id: 'conversationalBatchNumber',
                    name: 'Conversational Batch #'
                },
                {
                    id: 'virtualCompoundId',
                    name: 'Virtual Compound Id'
                },
                {
                    id: 'stereoisomer',
                    name: 'Stereoisomer Code',
                    type: 'select',
                    dictionary: 'Stereoisomer Code',
                    hasCustomItemProp: true,
                    values: function() {
                        return null;
                    },
                    width: '350px'
                },
                {
                    id: 'source',
                    name: 'Source',
                    type: 'select',
                    values: function() {
                        return sourceValues;
                    },
                    onChange: function(row) {
                        row.sourceDetail = {};
                    },
                    hideSelectValue: true,
                    actions: [_.extend({}, setSelectSourceValueAction, {
                        name: 'Set value for Source',
                        title: 'Source'
                    })]
                },
                {
                    id: 'sourceDetail',
                    name: 'Source Detail',
                    type: 'select',
                    values: function(row) {
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
                    name: 'External Supplier',
                    type: 'string',
                    onClick: function(data) {
                        editExternalSupplier(data.row);
                    },
                    actions: [{
                        name: 'Set value for External Supplier',
                        title: 'External Supplier',
                        rows: getProductBatches(),
                        action: function() {
                            editExternalSupplierForAllRows(getProductBatches());
                        }
                    }]
                },
                {
                    id: 'precursors',
                    name: 'Precursor/Reactant IDs',
                    type: 'input'
                },
                {
                    id: '$$healthHazards',
                    realId: 'healthHazards',
                    name: 'Health Hazards',
                    type: 'string',
                    onClick: function(data) {
                        editHealthHazards(data.row);
                    },
                    actions: [{
                        name: 'Set value for Health Hazards',
                        title: 'Health Hazards',
                        rows: getProductBatches(),
                        action: function() {
                            editHealthHazardsForAllRows(getProductBatches());
                        }
                    }]

                },
                {
                    id: 'compoundProtection',
                    name: 'Compound Protection',
                    type: 'select',
                    values: function() {
                        return compoundProtectionValues;
                    }
                },
                {
                    id: 'structureComments',
                    name: 'Structure Comments',
                    type: 'input',
                    bulkAssignment: true
                },
                {
                    id: 'registrationDate',
                    name: 'Registration Date',
                    format: function(val) {
                        return val ? $filter('date')(val, 'MMM DD, YYYY HH:mm:ss z') : null;
                    }
                }, {
                    id: '$$residualSolvents',
                    realId: 'residualSolvents',
                    name: 'Residual Solvents',
                    type: 'string',
                    onClick: function(data) {
                        editResidualSolvents([data.row]);
                    },
                    actions: [{
                        name: 'Set value for Residual Solvents',
                        title: 'Residual Solvents',
                        rows: getProductBatches(),
                        action: function() {
                            editResidualSolvents(getProductBatches());
                        }
                    }]
                }, {
                    id: '$$solubility',
                    realId: 'solubility',
                    name: 'Solubility in Solvents',
                    type: 'string',
                    onClick: function(data) {
                        editSolubility([data.row]);
                    },
                    actions: [{
                        name: 'Set value for Solubility in Solvents',
                        title: 'Solubility in Solvents',
                        rows: getProductBatches(),
                        action: function() {
                            editSolubility(getProductBatches());
                        }
                    }]
                }, {
                    id: '$$storageInstructions',
                    realId: 'storageInstructions',
                    name: 'Storage Instructions',
                    type: 'string',
                    onClick: function(data) {
                        editStorageInstructions([data.row]);
                    },
                    actions: [{
                        name: 'Set value for Storage Instructions',
                        title: 'Storage Instructions',
                        rows: getProductBatches(),
                        action: function() {
                            editStorageInstructions(getProductBatches());
                        }
                    }]
                }, {
                    id: '$$handlingPrecautions',
                    realId: 'handlingPrecautions',
                    name: 'Handling Precautions',
                    type: 'string',
                    onClick: function(data) {
                        editHandlingPrecautions([data.row]);
                    },
                    actions: [{
                        name: 'Set value for Handling Precautions',
                        title: 'Handling Precautions',
                        rows: getProductBatches(),
                        action: function() {
                            editHandlingPrecautions(getProductBatches());
                        }
                    }]
                }, {
                    id: 'comments',
                    name: 'Batch Comments',
                    type: 'input'
                }, {
                    id: '$$batchType',
                    name: 'Intermediate/Test Compound',
                    type: 'select',
                    values: function() {
                        return compounds;
                    },
                    onClose: function(data) {
                        var r = data.row;
                        if (!r.$$batchType) {
                            return;
                        }
                        r.batchType = r.$$batchType.name;
                    }
                }
            ];
        }

        function editResidualSolvents(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    row.residualSolvents = result;
                });
                EntitiesBrowser.setCurrentFormDirty();
            };
            var data = rows.length === 1 ? rows[0].residualSolvents : {};
            InfoEditor.editResidualSolvents(data || {}, callback);
        }

        function editSolubility(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    row.solubility = result;
                });
                EntitiesBrowser.setCurrentFormDirty();
            };
            if (rows.length === 1) {
                InfoEditor.editSolubility(rows[0].solubility, callback);
            }
        }

        function editHandlingPrecautions(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    row.handlingPrecautions = result;
                });
                EntitiesBrowser.setCurrentFormDirty();
            };
            var data = rows.length === 1 ? rows[0].handlingPrecautions : {};
            InfoEditor.editHandlingPrecautions(data || {}, callback);
        }

        function editStorageInstructions(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    row.storageInstructions = result;
                });
            };
            var data = rows.length === 1 ? rows[0].storageInstructions : {};
            InfoEditor.editStorageInstructions(data || {}, callback);
        }

        function editPurity(row) {
            InfoEditor.editPurity(row.purity, function(result) {
                row.purity = result;
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editPurityForAllRows(rows) {
            InfoEditor.editPurity({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.purity = angular.copy(result);
                    }
                });
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editMeltingPoint(row) {
            InfoEditor.editMeltingPoint(row.meltingPoint, function(result) {
                row.meltingPoint = result;
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editMeltingPointForAllRows(rows) {
            InfoEditor.editMeltingPoint({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.meltingPoint = angular.copy(result);
                    }
                });
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editExternalSupplier(row) {
            InfoEditor.editExternalSupplier(row.externalSupplier, function(result) {
                row.externalSupplier = result;
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editExternalSupplierForAllRows(rows) {
            InfoEditor.editExternalSupplier({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.externalSupplier = angular.copy(result);
                    }
                });
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editHealthHazards(row) {
            InfoEditor.editHealthHazards(row.healthHazards, function(result) {
                row.healthHazards = result;
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function editHealthHazardsForAllRows(rows) {
            InfoEditor.editHealthHazards({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.healthHazards = angular.copy(result);
                    }
                });
                EntitiesBrowser.setCurrentFormDirty();
            });
        }

        function syncWithIntendedProducts() {
            ProductBatchSummaryOperations.syncWithIntendedProducts();
        }

        function updatePrecursor() {
            if (!getStoicTable()) {
                return;
            }
            _.findWhere(vm.columns, {
                id: 'precursors'
            }).readonly = true;
            var precursors = vm.share.stoichTable.reactants.filter(function(r) {
                return (r.compoundId || r.fullNbkBatch) && r.rxnRole && r.rxnRole.name === 'REACTANT';
            })
                .map(function(r) {
                    return r.compoundId || r.fullNbkBatch;
                }).join(', ');
            _.each(getProductBatches(), function(batch) {
                batch.precursors = precursors;
            });
        }

        function getStoicTable() {
            return stoichTable;
        }

        function setStoicTable(table) {
            stoichTable = table;
            ProductBatchSummaryOperations.setStoicTable(stoichTable);
        }

        function isEditable(row, columnId) {
            var rowResult = !(RegistrationUtil.isRegistered(row));
            if (rowResult && columnId === 'precursors' && vm.share.stoichTable) {
                return false;
            }

            return rowResult;
        }

        function onRowSelected(row) {
            vm.share.selectedRow = row || null;
            if (row) {
                $rootScope.$broadcast('batch-summary-row-selected', {
                    row: row
                });
            } else {
                $rootScope.$broadcast('batch-summary-row-deselected');
            }
            $log.debug(row);
        }

        function isIntendedSynced() {
            var intended = ProductBatchSummaryOperations.getIntendedNotInActual();

            return intended ? !intended.length : true;
        }

        function duplicateBatch() {
            ProductBatchSummaryOperations.duplicateBatch();
        }

        function addNewBatch() {
            ProductBatchSummaryOperations.addNewBatch().then(function(batch) {
                vm.onRowSelected(batch);
            });
        }

        function isHasCheckedRows() {
            return !!_.find(getProductBatches(), function(item) {
                return item.select;
            });
        }

        function deleteBatches() {
            ProductBatchSummaryOperations.deleteBatches();
            if (vm.share.selectedRow && vm.share.selectedRow.select) {
                vm.onRowSelected(null);
            }
        }

        function importSDFile() {
            vm.importLoading = true;
            ProductBatchSummaryOperations.importSDFile(function() {
                vm.importLoading = false;
            });
        }

        function exportSDFile() {
            ProductBatchSummaryOperations.exportSDFile();
        }

        function registerBatches() {
            vm.loading = ProductBatchSummaryOperations.registerBatches();
        }

        function initStructure(batches, structureType) {
            var changed;
            _.each(batches, function(row) {
                if (row.structure) {
                    return;
                }
                var type = structureType || 'molecule';
                var model = vm.model[type] || {};
                changed = true;
                model.image = null;
                model.structureMolfile = null;
                model.structureId = null;

                row.structure = row.structure || {};
                row.structure.image = null;
                row.structure.structureType = type;
                row.structure.molfile = null;
                row.structure.structureId = null;
            });
            if (changed) {
                CalculationService.recalculateStoich();
            }
        }

        function getBatchType(batch) {
            if (!batch.batchType) {
                return null;
            }

            return compounds[0].name === batch.batchType ? compounds[0] : compounds[1];
        }

        function bindEvents() {
            $scope.$watch('vm.share.stoichTable', function(table) {
                setStoicTable(table);
                updatePrecursor();
            }, true);

            $scope.$watch('vm.model.productBatchSummary.batches', function(batches) {
                _.each(batches, function(batch) {
                    batch.$$purity = batch.purity ? batch.purity.asString : null;
                    batch.$$externalSupplier = batch.externalSupplier ? batch.externalSupplier.asString : null;
                    batch.$$meltingPoint = batch.meltingPoint ? batch.meltingPoint.asString : null;
                    batch.$$healthHazards = batch.healthHazards ? batch.healthHazards.asString : null;
                    batch.$$batchType = getBatchType(batch);
                });
                vm.share.actualProducts = batches;
                ProductBatchSummaryCache.setProductBatchSummary(batches);
                updatePrecursor();
                initStructure(batches);
            }, true);

            $scope.$watch('vm.isHasRegService', function(val) {
                _.findWhere(vm.columns, {
                    id: 'conversationalBatchNumber'
                }).isVisible = val;
                _.findWhere(vm.columns, {
                    id: 'registrationDate'
                }).isVisible = val;
                _.findWhere(vm.columns, {
                    id: 'registrationStatus'
                }).isVisible = val;
            });

            $scope.$watch(function() {
                return vm.showStructuresColumn.isVisible;
            }, function(val) {
                vm.onShowStructure({
                    isVisible: val
                });
            });

            $scope.$on('product-batch-structure-changed', function(event, row) {
                var resetMolInfo = function() {
                    row.formula = null;
                    row.molWeight = null;
                    CalculationService.calculateProductBatch({
                        row: row, column: 'totalWeight'
                    });
                };

                var getInfoCallback = function(molInfo) {
                    row.formula = molInfo.data.molecularFormula;
                    row.molWeight = row.molWeight || {};
                    row.molWeight.value = molInfo.data.molecularWeight;
                    CalculationService.recalculateStoich();
                    CalculationService.calculateProductBatch({
                        row: row, column: 'totalWeight'
                    });
                };
                if (row.structure && row.structure.molfile) {
                    CalculationService.getMoleculeInfo(row, getInfoCallback, resetMolInfo);
                } else {
                    resetMolInfo();
                }
            });

            $scope.$on('product-batch-details-command', function(event, data) {
                if (angular.isFunction(vm[data.command])) {
                    vm[data.command]();
                }
            });

            $scope.$on('product-batch-summary-recalculated', function(event, data) {
                if (data.length === getProductBatches().length) {
                    _.each(getProductBatches(), function(batch, i) {
                        _.extend(batch, data[i]);
                    });
                }
            });

            $scope.$watch('vm.structureSize', function(newVal) {
                var column = _.find(vm.columns, function(item) {
                    return item.id === 'structure';
                });
                column.width = (500 * newVal) + 'px';
            });

            unbinds.push($rootScope.$on('batch-registration-status-changed', function(event, statuses) {
                _.each(statuses, function(status, fullNbkBatch) {
                    var batch = _.find(getProductBatches(), {
                        fullNbkBatch: fullNbkBatch
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
            }));

            $scope.$on('$destroy', function() {
                _.each(unbinds, function(unbind) {
                    unbind();
                });
            });
        }
    }
})();
