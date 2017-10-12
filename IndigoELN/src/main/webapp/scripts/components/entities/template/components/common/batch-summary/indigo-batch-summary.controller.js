(function() {
    angular
        .module('indigoeln')
        .controller('IndigoBatchSummaryController', IndigoBatchSummaryController);

    /* @ngInject */
    function IndigoBatchSummaryController($scope, RegistrationService, ProductBatchSummaryOperations, batchHelper) {
        var vm = this;

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

        function onClose(column, data) {
            batchHelper.close(column, data);
        }

        function getDefaultColumns() {
            return [
                batchHelper.columns.structure,
                batchHelper.columns.nbkBatch,
                batchHelper.columns.registrationStatus,
                batchHelper.columns.select,
                batchHelper.columns.totalWeight,
                batchHelper.columns.totalVolume,
                batchHelper.columns.mol,
                batchHelper.columns.theoWeight,
                batchHelper.columns.theoMoles,
                batchHelper.columns.yield,
                batchHelper.columns.compoundState,
                batchHelper.columns.saltCode,
                batchHelper.columns.saltEq,
                batchHelper.columns.purity,
                batchHelper.columns.$$meltingPoint,
                batchHelper.columns.molWeight,
                batchHelper.columns.formula,
                batchHelper.columns.conversationalBatchNumber,
                batchHelper.columns.virtualCompoundId,
                batchHelper.columns.stereoisomer,
                batchHelper.columns.source,
                batchHelper.columns.sourceDetail,
                batchHelper.columns.$$externalSupplier,
                getPrecursorColumn(),
                batchHelper.columns.$$healthHazards,
                batchHelper.columns.compoundProtection,
                batchHelper.columns.structureComments,
                batchHelper.columns.registrationDate,
                batchHelper.columns.$$residualSolvents,
                batchHelper.columns.$$solubility,
                batchHelper.columns.$$storageInstructions,
                batchHelper.columns.$$handlingPrecautions,
                batchHelper.columns.comments,
                batchHelper.columns.$$batchType
            ];
        }

        function getPrecursorColumn() {
            return _.extend({}, batchHelper.columns.precursors, {readonly: vm.isExistStoichTable});
        }

        function syncWithIntendedProducts() {
            vm.batchOperation = ProductBatchSummaryOperations.syncWithIntendedProducts()
                .then(successAddedBatches);
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
            return !!_.find(vm.batches, function(item) {
                return item.$$select;
            });
        }

        function importSDFile() {
            vm.batchOperation = ProductBatchSummaryOperations.importSDFile().then(successAddedBatches);
        }

        function exportSDFile() {
            ProductBatchSummaryOperations.exportSDFile(vm.batches);
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

            return batchHelper.compounds[0].name === batch.batchType ?
                batchHelper.compounds[0]
                : batchHelper.compounds[1];
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
