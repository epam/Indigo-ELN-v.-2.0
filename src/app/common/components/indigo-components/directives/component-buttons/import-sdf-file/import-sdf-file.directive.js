/* eslint no-shadow: "off"*/
var template = require('./import-sdf-file.html');

function importSdfFile() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'importSdfFile'],
        scope: {
            isReadonly: '='
        },
        template: template,
        controller: ImportSdfFileController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };

    /* @ngInject */
    function ImportSdfFileController(productBatchSummaryOperations, notifyService, $q, $scope) {
        var vm = this;

        init();

        function init() {
            vm.importSdfFile = importSdfFile;

            bindEvents();
        }

        function importSdfFile() {
            vm.indigoComponents.batchOperation = productBatchSummaryOperations
                .importSDFile(vm.indigoComponents.experiment)
                .then(function(batches) {
                    if (batches.length > 0) {
                        notifyService.info(batches.length + ' batches successfully imported');

                        return batches;
                    }

                    return $q.reject('Error! Batch(es) not imported');
                })
                .then(successAddedBatches)
                .catch(function(e) {
                    if (e) {
                        notifyService.error(e);
                    }
                });
        }

        function successAddedBatches(batches) {
            if (batches.length) {
                _.forEach(batches, function(batch) {
                    vm.indigoComponents.onAddedBatch(batch);
                });
                vm.indigoComponents.onSelectBatch(_.last(batches));
            }
        }

        function bindEvents() {
            $scope.$on('$destroy', function() {
                productBatchSummaryOperations.closeImportSDFileDialog();
            });
        }
    }
}

module.exports = importSdfFile;
