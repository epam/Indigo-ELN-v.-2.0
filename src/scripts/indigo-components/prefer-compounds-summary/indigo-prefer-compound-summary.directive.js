(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoPreferredCompoundsSummary', indigoPreferredCompoundsSummary);

    function indigoPreferredCompoundsSummary() {
        return {
            restrict: 'E',
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
                onAddedBatch: '&',
                onSelectBatch: '&',
                onRemoveBatches: '&',
                onChanged: '&'
            },
            controller: IndigoPreferredCompoundsSummaryController,
            controllerAs: 'vm',
            bindToController: true,
            templateUrl: 'scripts/indigo-components/prefer-compounds-summary/prefer-compound-summary.html'
        };

        /* @ngInject */
        function IndigoPreferredCompoundsSummaryController() {
            var vm = this;

            vm.model = vm.model || {};
            vm.experiment = vm.experiment || {};
            vm.structureSize = 0.3;
            vm.isStructureVisible = false;
        }
    }
})();
