(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('addNewBatch', addNewBatchDirective);

    /* @ngInject */
    function addNewBatchDirective() {
        return {
            restrict: 'E',
            scope: {
                batchOperation: '=',
                isReadonly: '=',
                onAddedBatch: '&',
                onSelectBatch: '&'
            },
            templateUrl: 'scripts/app/common/directives/component-buttons/add-new-batch/add-new-batch.html',
            controller: AddNewBatchController,
            controllerAs: 'vm',
            bindToController: true,
            link: function($scope, $element, $attr, controllers) {
                $element.addClass('component-button');
            }
        };
    }

    AddNewBatchController.$inject = ['productBatchSummaryOperations'];

    function AddNewBatchController(productBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.addNewBatch = addNewBatch;
        }

        function addNewBatch() {
            vm.batchOperation = productBatchSummaryOperations.addNewBatch().then(successAddedBatch);
        }

        function successAddedBatch(batch) {
            vm.onAddedBatch({batch: batch});
            vm.onSelectBatch({batch: batch});
        }
    }
})();
