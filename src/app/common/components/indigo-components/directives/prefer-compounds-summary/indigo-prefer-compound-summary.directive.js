var template = require('./prefer-compound-summary.html');

function indigoPreferredCompoundsSummary() {
    return {
        restrict: 'E',
        scope: {
            batches: '=',
            batchesTrigger: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            experimentName: '=',
            structureSize: '=',
            isHideColumnSettings: '=',
            isReadonly: '=',
            batchOperation: '=',
            onAddedBatch: '&',
            onSelectBatch: '&',
            onRemoveBatches: '&',
            onChanged: '&'
        },
        controller: IndigoPreferredCompoundsSummaryController,
        controllerAs: 'vm',
        bindToController: true,
        template: template
    };

    function IndigoPreferredCompoundsSummaryController() {
        var vm = this;

        vm.experiment = vm.experiment || {};
        vm.structureSize = 0.3;
        vm.isStructureVisible = false;
    }
}

module.exports = indigoPreferredCompoundsSummary;

