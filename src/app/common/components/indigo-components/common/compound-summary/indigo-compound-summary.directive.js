var template = require('./compound-summary.html');

function indigoCompoundSummary() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            model: '=',
            batches: '=',
            batchesTrigger: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            experimentName: '=',
            structureSize: '=',
            isHideColumnSettings: '=',
            isReadonly: '=',
            batchOperation: '=',
            onShowStructure: '&',
            onAddedBatch: '&',
            onSelectBatch: '&',
            onRemoveBatches: '&',
            onChanged: '&'
        },
        bindToController: true,
        controllerAs: 'vm',
        controller: IndigoCompoundSummaryController
    };
}

IndigoCompoundSummaryController.$inject = ['$scope', 'batchHelper'];

function IndigoCompoundSummaryController($scope, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.model = vm.model || {};
        vm.columns = getDefaultColumns();
        vm.hasCheckedRows = batchHelper.hasCheckedRow;
        vm.vnv = angular.noop;
        vm.registerVC = angular.noop;
        vm.onBatchOperationChanged = onBatchOperationChanged;
        vm.isBatchLoading = false;
        vm.onClose = batchHelper.close;
        vm.onChangedVisibleColumn = onChangedVisibleColumn;

        bindEvents();
    }

    function onChangedVisibleColumn(column, isVisible) {
        if (column.id === 'structure') {
            vm.onShowStructure({isVisible: isVisible});
        }
    }

    function getDefaultColumns() {
        return [
            batchHelper.columns.structure,
            batchHelper.columns.nbkBatch,
            batchHelper.columns.select,
            batchHelper.columns.virtualCompoundId,
            batchHelper.columns.molWeight,
            batchHelper.columns.formula,
            batchHelper.columns.stereoisomer,
            batchHelper.columns.structureComments,
            batchHelper.columns.saltCode,
            batchHelper.columns.saltEq
        ];
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

module.exports = indigoCompoundSummary;
