var template = require('./add-new-batch.html');

function addNewBatchDirective() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'addNewBatch'],
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
        link: function($scope, $element, $attr, controllers) {
            controllers[1].indigoComponents = controllers[0];
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
        vm.batchOperation = productBatchSummaryOperations
            .addNewBatch(vm.indigoComponents.experiment)
            .then(successAddedBatch);
    }

    function successAddedBatch(batch) {
        vm.onAddedBatch({batch: batch});
        vm.onSelectBatch({batch: batch});
    }
}

module.exports = addNewBatchDirective;
