(function() {
    angular
        .module('indigoeln.componentsModule')
        .controller('IndigoBatchSummaryController', IndigoBatchSummaryController);

    /* @ngInject */
    function IndigoBatchSummaryController($scope, registrationService, productBatchSummaryOperations, batchHelper) {
        var vm = this;

        init();

        function init() {
            vm.loading = false;
            vm.model = vm.model || {};

            registrationService.info({}).$promise.then(function(info) {
                var hasRegService = _.isArray(info) && info.length > 0;

                vm.columns = getDefaultColumns(hasRegService);
            });

            vm.syncWithIntendedProducts = syncWithIntendedProducts;
            vm.isIntendedSynced = isIntendedSynced;
            vm.hasCheckedRows = batchHelper.hasCheckedRow;
            vm.registerBatches = registerBatches;
            vm.isBatchLoading = false;
            vm.onBatchOperationChanged = onBatchOperationChanged;
            vm.onClose = batchHelper.close;
            vm.onChangedVisibleColumn = onChangedVisibleColumn;

            bindEvents();
        }

        function onChangedVisibleColumn(column, isVisible) {
            if (column.id === 'structure') {
                vm.onShowStructure({column: column, isVisible: isVisible});
            }
        }

        function updateColumnVisible(column, isVisible) {
            return _.extend({}, column, {isVisible: isVisible});
        }

        function getDefaultColumns(hasRegService) {
            return [
                batchHelper.columns.structure,
                batchHelper.columns.nbkBatch,
                updateColumnVisible(batchHelper.columns.registrationStatus, hasRegService),
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
                updateColumnVisible(batchHelper.columns.conversationalBatchNumber, hasRegService),
                batchHelper.columns.virtualCompoundId,
                batchHelper.columns.stereoisomer,
                batchHelper.columns.source,
                batchHelper.columns.sourceDetail,
                batchHelper.columns.$$externalSupplier,
                getPrecursorColumn(),
                batchHelper.columns.$$healthHazards,
                batchHelper.columns.compoundProtection,
                batchHelper.columns.structureComments,
                updateColumnVisible(batchHelper.columns.registrationDate, hasRegService),
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
            vm.batchOperation = productBatchSummaryOperations.syncWithIntendedProducts()
                .then(successAddedBatches);
        }

        function isIntendedSynced() {
            var intended = productBatchSummaryOperations.getIntendedNotInActual();

            return intended ? !intended.length : true;
        }

        function successAddedBatches(batches) {
            if (batches.length) {
                _.forEach(batches, function(batch) {
                    vm.onAddedBatch({batch: batch});
                });
                vm.onChanged();
                vm.onSelectBatch({batch: _.last(batches)});
            }
        }

        function registerBatches() {
            vm.loading = vm.saveExperimentFn().then(function() {
                return productBatchSummaryOperations.registerBatches(batchHelper.getCheckedBatches(vm.batches));
            });
        }

        function onBatchOperationChanged(completed) {
            vm.isBatchLoading = completed;
        }

        function bindEvents() {
            $scope.$watch('vm.structureSize', function(newVal) {
                var column = _.find(vm.columns, function(item) {
                    return item.id === 'structure';
                });
                if (column) {
                    column.width = (500 * newVal) + 'px';
                }
            });
        }
    }
})();
