(function() {
    angular
        .module('indigoeln')
        .controller('IndigoCompoundSummaryController', IndigoCompoundSummaryController);

    /* @ngInject */
    function IndigoCompoundSummaryController($scope, $log, ProductBatchSummaryOperations, AppValues, CalculationService) {
        var vm = this;
        var saltCodeValues = AppValues.getSaltCodeValues();

        init();

        function init() {
            vm.model = vm.model || {};
            vm.columns = [
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
                    id: '$$select',
                    name: 'Select',
                    type: 'boolean',
                    noDisablable: true,
                    noDirty: true,
                    actions: [
                        {
                            name: 'Select All',
                            action: function() {
                                _.each(getBatches(), function(row) {
                                    row.$$select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function() {
                                _.each(getBatches(), function(row) {
                                    row.$$select = false;
                                });
                            }
                        }
                    ]
                },
                {
                    id: 'virtualCompoundId', name: ' Virtual Compound ID', type: 'input'
                },
                {
                    id: 'molWeight', name: 'Mol Wgt', type: 'scalar'
                },
                {
                    id: 'formula', name: 'Mol Formula', type: 'input', readonly: true
                },
                {
                    id: 'stereoisomer',
                    name: 'Stereoisomer',
                    type: 'select',
                    dictionary: 'Stereoisomer Code',
                    values: function() {
                        return null;
                    },
                    width: '350px'
                },
                {
                    id: 'structureComments',
                    name: 'Structure Comments',
                    type: 'input',
                    bulkAssignment: true
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
                }
            ];

            vm.onRowSelected = onRowSelected;
            vm.deleteBatches = deleteBatches;
            vm.addNewBatch = addNewBatch;
            vm.duplicateBatches = duplicateBatches;
            vm.registerVC = registerVC;
            vm.importSDFile = importSDFile;
            vm.exportSDFile = exportSDFile;
            vm.isHasCheckedRows = isHasCheckedRows;
            vm.vnv = vnv;
            vm.onBatchOperationChanged = onBatchOperationChanged;
            vm.isBatchLoading = false;

            bindEvents();
        }

        function recalculateSalt(reagent) {
            CalculationService.recalculateSalt(reagent).then(function() {
                CalculationService.recalculateStoich();
            });
        }

        function duplicateBatches() {
            vm.batchOperation = ProductBatchSummaryOperations.duplicateBatches(getCheckedBatches())
                .then(successAddedBatches);
        }

        function addNewBatch() {
            vm.batchOperation = ProductBatchSummaryOperations.addNewBatch().then(successAddedBatch);
        }

        function onRowSelected(row) {
            vm.onSelectBatch({batch: row});
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

        function successAddedBatch(batch) {
            vm.onAddedBatch({batch: batch});
            vm.onChanged();
            vm.onRowSelected(batch);
        }

        function getCheckedBatches() {
            return _.filter(vm.batches, function(batch) {
                return batch.$$select;
            });
        }

        function isHasCheckedRows() {
            return !!_.find(getBatches(), function(item) {
                return item.$$select;
            });
        }

        function deleteBatches() {
            vm.onRemoveBatches({batches: getCheckedBatches()});
        }

        function importSDFile() {
            vm.batchOperation = ProductBatchSummaryOperations.importSDFile().then(successAddedBatches);
        }

        function exportSDFile() {
            ProductBatchSummaryOperations.exportSDFile(getBatches());
        }

        function getBatches() {
            return vm.batches;
        }

        function registerVC() {

        }

        function vnv() {
            $log.debug('VnV');
        }

        function onBatchOperationChanged(completed) {
            vm.isBatchLoading = completed;
        }

        function bindEvents() {
            $scope.$watch('vm.structureSize', function(newVal) {
                var column = _.find(vm.columns, function(item) {
                    return item.id === 'structure';
                });
                column.width = (500 * newVal) + 'px';
            });
        }
    }
})();
