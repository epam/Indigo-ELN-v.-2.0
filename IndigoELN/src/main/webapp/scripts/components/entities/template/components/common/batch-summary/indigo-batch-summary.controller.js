(function() {
    angular
        .module('indigoeln')
        .controller('IndigoBatchSummaryController', IndigoBatchSummaryController);

    /* @ngInject */
    function IndigoBatchSummaryController($scope, InfoEditor, RegistrationUtil, $uibModal, RegistrationService,
                                          ProductBatchSummaryOperations, batchHelper) {
        var vm = this;
        var compounds = [
            {name: 'Intermediate'},
            {name: 'Test Compound'}
        ];

        init();

        function init() {
            vm.loading = false;
            vm.model = vm.model || {};
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
            vm.isHasCheckedRows = isHasCheckedRows;
            vm.duplicateBatches = duplicateBatches;
            vm.exportSDFile = exportSDFile;
            vm.registerBatches = registerBatches;
            vm.isBatchLoading = false;
            vm.onBatchOperationChanged = onBatchOperationChanged;
            vm.onClose = onClose;

            bindEvents();
        }

        function getProductBatches() {
            return vm.batches;
        }

        function openProductBatchSummaryModal() {
            var that = this;
            $uibModal.open({
                templateUrl: 'scripts/components/entities/template/components/product-batch-summary/product-batch-summary-set-source.html',
                controller: 'ProductBatchSummarySetSourceController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    name: function() {
                        return that.title;
                    }
                }
            }).result.then(function(result) {
                _.each(getProductBatches(), function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.source = result.source;
                        row.sourceDetail = result.sourceDetail;
                    }
                });
            });
        }

        function onClose(column, data) {
            batchHelper.close(column, data);
        }

        function getDefaultColumns() {
            return [
                batchHelper.columns.structure,
                batchHelper.columns.nbkBatch,
                batchHelper.columns.registrationStatus,
                {
                    id: '$$select',
                    name: 'Select',
                    type: 'boolean',
                    noDisablable: true,
                    noDirty: true,
                    actions: [
                        {
                            name: 'Select All',
                            action: function() {
                                _.each(getProductBatches(), function(row) {
                                    row.$$select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function() {
                                _.each(getProductBatches(), function(row) {
                                    row.$$select = false;
                                });
                            }
                        }
                    ]
                },
                batchHelper.columns.totalWeight,
                batchHelper.columns.totalVolume,
                batchHelper.columns.mol,
                batchHelper.columns.theoWeight,
                batchHelper.columns.theoMoles,
                batchHelper.columns.yield,
                batchHelper.columns.compoundState,
                batchHelper.columns.saltCode,
                batchHelper.columns.saltEq,
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
                batchHelper.columns.molWeight,
                batchHelper.columns.formula,
                batchHelper.columns.conversationalBatchNumber,
                batchHelper.columns.virtualCompoundId,
                batchHelper.columns.stereoisomer,
                {
                    id: 'source',
                    name: 'Source',
                    type: 'select',
                    dictionary: 'Source',
                    hideSelectValue: true,
                    actions: [{
                        name: 'Set value for Source',
                        title: 'Source',
                        action: openProductBatchSummaryModal
                    }]
                },
                {
                    id: 'sourceDetail',
                    name: 'Source Detail',
                    type: 'select',
                    dictionary: 'Source Details',
                    hideSelectValue: true,
                    actions: [{
                        name: 'Set value for Source Detail',
                        title: 'Source Detail',
                        action: openProductBatchSummaryModal
                    }]
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
                batchHelper.columns.precursors,
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
                batchHelper.columns.compoundProtection,
                batchHelper.columns.structureComments,
                batchHelper.columns.registrationDate,
                {
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
                },
                {
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
                },
                {
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
                },
                {
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
                },
                batchHelper.columns.comments,
                batchHelper.columns.$$batchType
            ];
        }

        function editResidualSolvents(rows) {
            var data = rows.length === 1 ? rows[0].residualSolvents : {};

            InfoEditor.editResidualSolvents(data).then(function(result) {
                _.forEach(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.residualSolvents = result;
                    }
                });
            });
        }

        function editSolubility(rows) {
            var data = rows.length === 1 ? rows[0].solubility : {};

            InfoEditor.editSolubility(data, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.solubility = result;
                    }
                });
            });
        }

        function editHandlingPrecautions(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.handlingPrecautions = result;
                    }
                });
            };
            var data = rows.length === 1 ? rows[0].handlingPrecautions : {};
            InfoEditor.editHandlingPrecautions(data || {}, callback);
        }

        function editStorageInstructions(rows) {
            var callback = function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.storageInstructions = result;
                    }
                });
            };
            var data = rows.length === 1 ? rows[0].storageInstructions : {};
            InfoEditor.editStorageInstructions(data || {}, callback);
        }

        function editPurity(row) {
            InfoEditor.editPurity(row.purity, function(result) {
                row.purity = result;
            });
        }

        function editPurityForAllRows(rows) {
            InfoEditor.editPurity({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.purity = angular.copy(result);
                    }
                });
            });
        }

        function editMeltingPoint(row) {
            InfoEditor.editMeltingPoint(row.meltingPoint, function(result) {
                row.meltingPoint = result;
            });
        }

        function editMeltingPointForAllRows(rows) {
            InfoEditor.editMeltingPoint({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.meltingPoint = angular.copy(result);
                    }
                });
            });
        }

        function editExternalSupplier(row) {
            InfoEditor.editExternalSupplier(row.externalSupplier, function(result) {
                row.externalSupplier = result;
            });
        }

        function editExternalSupplierForAllRows(rows) {
            InfoEditor.editExternalSupplier({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.externalSupplier = angular.copy(result);
                    }
                });
            });
        }

        function editHealthHazards(row) {
            InfoEditor.editHealthHazards(row.healthHazards, function(result) {
                row.healthHazards = result;
            });
        }

        function editHealthHazardsForAllRows(rows) {
            InfoEditor.editHealthHazards({}, function(result) {
                _.each(rows, function(row) {
                    if (!RegistrationUtil.isRegistered(row)) {
                        row.healthHazards = angular.copy(result);
                    }
                });
            });
        }

        function syncWithIntendedProducts() {
            vm.batchOperation = ProductBatchSummaryOperations.syncWithIntendedProducts()
                .then(successAddedBatches);
        }

        function isEditable(row, columnId) {
            var rowResult = !(RegistrationUtil.isRegistered(row));
            if (rowResult && columnId === 'precursors' && vm.stoichTable) {
                return false;
            }

            return rowResult;
        }

        function onRowSelected(row) {
            vm.onSelectBatch({batch: row});
        }

        function isIntendedSynced() {
            var intended = ProductBatchSummaryOperations.getIntendedNotInActual();

            return intended ? !intended.length : true;
        }

        function duplicateBatches() {
            vm.batchOperation = ProductBatchSummaryOperations.duplicateBatches(getCheckedBatches())
                .then(successAddedBatches);
        }

        function successAddedBatches(batches) {
            if (batches.length) {
                _.forEach(batches, function(batch) {
                    vm.onAddedBatch({batch: batch});
                });
                vm.onChanged();
                vm.onRowSelected(_.last(batches));
            }
        }

        function getCheckedBatches() {
            return _.filter(vm.batches, function(batch) {
                return batch.$$select;
            });
        }

        function isHasCheckedRows() {
            return !!_.find(getProductBatches(), function(item) {
                return item.$$select;
            });
        }

        function importSDFile() {
            vm.batchOperation = ProductBatchSummaryOperations.importSDFile().then(successAddedBatches);
        }

        function exportSDFile() {
            ProductBatchSummaryOperations.exportSDFile(getProductBatches());
        }

        function registerBatches() {
            vm.loading = vm.saveExperimentFn().then(function() {
                return ProductBatchSummaryOperations.registerBatches(getCheckedBatches());
            });
        }

        function getBatchType(batch) {
            if (!batch.batchType) {
                return null;
            }

            return compounds[0].name === batch.batchType ? compounds[0] : compounds[1];
        }

        function onBatchOperationChanged(completed) {
            vm.isBatchLoading = completed;
        }

        function bindEvents() {
            $scope.$watch('vm.batchesTrigger', function() {
                _.each(vm.batches, function(batch) {
                    batch.$$purity = batch.purity ? batch.purity.asString : null;
                    batch.$$externalSupplier = batch.externalSupplier ? batch.externalSupplier.asString : null;
                    batch.$$meltingPoint = batch.meltingPoint ? batch.meltingPoint.asString : null;
                    batch.$$healthHazards = batch.healthHazards ? batch.healthHazards.asString : null;
                    batch.$$batchType = getBatchType(batch);
                });
            }, true);

            $scope.$watch('vm.isHasRegService', function(val) {
                _.find(vm.columns, {id: 'conversationalBatchNumber'}).isVisible = val;
                _.find(vm.columns, {id: 'registrationDate'}).isVisible = val;
                _.find(vm.columns, {id: 'registrationStatus'}).isVisible = val;
            });

            $scope.$watch(function() {
                return vm.showStructuresColumn.isVisible;
            }, function(val) {
                vm.onShowStructure({isVisible: val});
            });

            $scope.$watch('vm.structureSize', function(newVal) {
                var column = _.find(vm.columns, function(item) {
                    return item.id === 'structure';
                });
                column.width = (500 * newVal) + 'px';
            });
        }
    }
})();
