(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('addNewBatch', addNewBatchDirective);

    /* @ngInject */
    function addNewBatchDirective() {
        return {
            restrict: 'E',
            replace: true,
            scope: {},
            require: ['addNewBatch', '^indigoComponents'],
            templateUrl: 'scripts/app/common/directives/component-buttons/add-new-batch/add-new-batch.html',
            controller: addNewBatchController,
            controllerAs: 'vm',
            bindToController: true,
            link: function($scope, $element, $attr, controllers) {
                var vm = controllers[0];

                vm.ComponentsCtrl = controllers[1];
            }
        };
    }

    addNewBatchController.$inject = ['ProductBatchSummaryOperations'];

    function addNewBatchController(ProductBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.addNewBatch = addNewBatch;
        }

        function addNewBatch() {
            vm.ComponentsCtrl.batchOperation = ProductBatchSummaryOperations.addNewBatch().then(successAddedBatch);
        }

        function successAddedBatch(batch) {
            vm.ComponentsCtrl.onAddedBatch(batch);
            vm.ComponentsCtrl.onSelectBatch(batch);
        }
    }
})();
