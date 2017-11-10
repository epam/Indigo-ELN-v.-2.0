var template = require('./add-new-batch.html');

function addNewBatchDirective() {
    return {
        restrict: 'E',
        scope: {
            batchOperation: '=',
            isReadonly: '=',
            onAddedBatch: '&',
            onSelectBatch: '&'
        },
        template: template,
        controller: AddNewBatchController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element) {
            $element.addClass('component-button');
        }
    };

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
}

module.exports = addNewBatchDirective;
