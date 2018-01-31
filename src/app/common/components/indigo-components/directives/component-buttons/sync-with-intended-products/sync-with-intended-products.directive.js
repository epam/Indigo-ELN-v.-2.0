var template = require('./sync-with-intended-products.html');

function syncWithIntendedProducts() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'syncWithIntendedProducts'],
        scope: {
            isReadonly: '='
        },
        template: template,
        controller: syncWithIntendedProductsController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };
}

syncWithIntendedProductsController.$inject = ['productBatchSummaryOperations'];

function syncWithIntendedProductsController(productBatchSummaryOperations) {
    var vm = this;

    init();

    function init() {
        vm.sync = sync;
        vm.isIntendedSynced = isIntendedSynced;
    }

    function isIntendedSynced() {
        var stoich = productBatchSummaryOperations.getStoichFromExperiment(vm.indigoComponents.experiment);
        var intended = productBatchSummaryOperations.getIntendedNotInActual(stoich);

        return intended ? !intended.length : true;
    }

    function sync() {
        vm.indigoComponents.batchOperation = productBatchSummaryOperations
            .syncWithIntendedProducts(vm.indigoComponents.experiment)
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

module.exports = syncWithIntendedProducts;
