var template = require('./batch-summary.html');

function indigoBatchSummary() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            batches: '=',
            isReadonly: '=',
            isHideColumnSettings: '=',
            structureSize: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            isExistStoichTable: '=',
            batchOperation: '=',
            onShowStructure: '&',
            saveExperimentFn: '&',
            onAddedBatch: '&',
            onSelectBatch: '&',
            onRemoveBatches: '&',
            onChanged: '&'
        },
        bindToController: true,
        controller: IndigoBatchSummaryController,
        controllerAs: 'vm'
    };
}

IndigoBatchSummaryController.$inject =
    ['$scope', 'registrationService', 'productBatchSummaryOperations', 'batchHelperService'];

function IndigoBatchSummaryController($scope, registrationService, productBatchSummaryOperations, batchHelperService) {
    var vm = this;

    init();

    function init() {
        vm.loading = false;
        vm.model = vm.model || {};

        registrationService.info({}).$promise.then(function(info) {
            vm.hasRegService = _.isArray(info) && info.length > 0;

            vm.columns = getDefaultColumns(vm.hasRegService);
        });

        vm.hasCheckedRows = batchHelperService.hasCheckedRow;
        vm.registerBatches = registerBatches;
        vm.isBatchLoading = false;
        vm.onBatchOperationChanged = onBatchOperationChanged;
        vm.onClose = batchHelperService.close;
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
            batchHelperService.columns.structure,
            batchHelperService.columns.nbkBatch,
            updateColumnVisible(batchHelperService.columns.registrationStatus, hasRegService),
            batchHelperService.columns.select,
            batchHelperService.columns.totalWeight,
            batchHelperService.columns.totalVolume,
            batchHelperService.columns.mol,
            batchHelperService.columns.theoWeight,
            batchHelperService.columns.theoMoles,
            batchHelperService.columns.yield,
            batchHelperService.columns.compoundState,
            batchHelperService.columns.saltCode,
            batchHelperService.columns.saltEq,
            batchHelperService.columns.purity,
            batchHelperService.columns.$$meltingPoint,
            batchHelperService.columns.molWeight,
            batchHelperService.columns.formula,
            updateColumnVisible(batchHelperService.columns.conversationalBatchNumber, hasRegService),
            batchHelperService.columns.virtualCompoundId,
            batchHelperService.columns.stereoisomer,
            batchHelperService.columns.source,
            batchHelperService.columns.sourceDetail,
            batchHelperService.columns.$$externalSupplier,
            getPrecursorColumn(),
            batchHelperService.columns.$$healthHazards,
            batchHelperService.columns.compoundProtection,
            batchHelperService.columns.structureComments,
            updateColumnVisible(batchHelperService.columns.registrationDate, hasRegService),
            batchHelperService.columns.$$residualSolvents,
            batchHelperService.columns.$$solubility,
            batchHelperService.columns.$$storageInstructions,
            batchHelperService.columns.$$handlingPrecautions,
            batchHelperService.columns.comments,
            batchHelperService.columns.$$batchType
        ];
    }

    function getPrecursorColumn() {
        return _.extend({}, batchHelperService.columns.precursors, {readonly: vm.isExistStoichTable});
    }

    function registerBatches() {
        vm.loading = vm.saveExperimentFn().then(function() {
            return productBatchSummaryOperations.registerBatches(batchHelperService.getCheckedBatches(vm.batches));
        });
    }

    function onBatchOperationChanged(completed) {
        vm.isBatchLoading = !completed;
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

module.exports = indigoBatchSummary;
