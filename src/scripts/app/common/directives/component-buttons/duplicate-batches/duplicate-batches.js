(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('duplicateBatches', duplicateBatchesDirective);

    /* @ngInject */
    function duplicateBatchesDirective() {
        return {
            restrict: 'E',
            require: ['^^indigoComponents', 'duplicateBatches'],
            scope: {
                isReadonly: '=',
                batches: '='
            },
            templateUrl: 'scripts/app/common/directives/component-buttons/duplicate-batches/duplicate-batches.html',
            controller: DuplicateBatchesController,
            controllerAs: 'vm',
            bindToController: true,
            link: function($scope, $element, $attr, controllers) {
                $element.addClass('component-button');
                controllers[1].indigoComponents = controllers[0];
            }
        };
    }

    DuplicateBatchesController.$inject = ['productBatchSummaryOperations', 'batchHelper'];

    function DuplicateBatchesController(productBatchSummaryOperations, batchHelper) {
        var vm = this;

        init();

        function init() {
            vm.duplicateBatches = duplicateBatches;
        }

        function duplicateBatches() {
            vm.indigoComponents.batchOperation = productBatchSummaryOperations.duplicateBatches(batchHelper.getCheckedBatches(vm.batches))
                .then(successAddedBatches);
        }

        function successAddedBatches(batches) {
            if (batches.length) {
                _.forEach(batches, function(batch) {
                    vm.indigoComponents.onAddedBatch(batch);
                });
                vm.indigoComponents.onSelectBatch(_.last(batches));
            }
        }
    }
})();
