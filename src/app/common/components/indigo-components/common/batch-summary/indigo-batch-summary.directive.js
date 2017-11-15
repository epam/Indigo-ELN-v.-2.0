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

IndigoBatchSummaryController.$inject = ['$scope', 'registrationUtil', 'batchHelper'];

function IndigoBatchSummaryController($scope, registrationUtil, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.loading = false;

        registrationUtil.hasRegistrationService().then(function(hasRegService) {
            vm.hasRegService = hasRegService;

            vm.columns = getDefaultColumns(vm.hasRegService);
        });

        vm.hasCheckedRows = batchHelper.hasCheckedRow;
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
