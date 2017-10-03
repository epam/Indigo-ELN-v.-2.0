(function() {
    angular
        .module('indigoeln')
        .controller('IndigoCompoundSummaryController', IndigoCompoundSummaryController);

    /* @ngInject */
    function IndigoCompoundSummaryController($scope, ProductBatchSummaryOperations, batchHelper) {
        var vm = this;

        init();

        function init() {
            vm.model = vm.model || {};
            vm.columns = getDefaultColumns();

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
            vm.onClose = onClose;

            bindEvents();
        }

        function onClose(column, data) {
            batchHelper.close(column, data);
        }

        function getDefaultColumns() {
            return [
                batchHelper.columns.structure,
                batchHelper.columns.nbkBatch,
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
                batchHelper.columns.virtualCompoundId,
                batchHelper.columns.molWeight,
                batchHelper.columns.formula,
                batchHelper.columns.stereoisomer,
                batchHelper.columns.structureComments,
                batchHelper.columns.saltCode,
                batchHelper.columns.saltEq
            ];
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
