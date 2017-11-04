/* @ngInject */
function importSdfFileDirective() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'importSdfFile'],
        scope: {
            isReadonly: '='
        },
        template: require('./import-sdf-file.html'),
        controller: ImportSdfFileController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };

    function ImportSdfFileController(productBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.importSdfFile = importSdfFile;
        }

        function importSdfFile() {
            vm.indigoComponents.batchOperation = productBatchSummaryOperations.importSDFile().then(successAddedBatches);
        }

        function successAddedBatches(batches) {
            if (batches.length) {
                _.forEach(batches, function (batch) {
                    vm.indigoComponents.onAddedBatch(batch);
                });
                vm.indigoComponents.onSelectBatch(_.last(batches));
            }
        }
    }
}

module.exports = importSdfFileDirective;
