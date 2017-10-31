(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('exportSdfFile', exportSdfFileDirective);

    /* @ngInject */
    function exportSdfFileDirective() {
        return {
            restrict: 'E',
            require: ['^^indigoComponents', 'exportSdfFile'],
            scope: {
                isReadonly: '=',
                isSelectedBatch: '=?'
            },
            templateUrl: 'scripts/app/common/directives/component-buttons/export-sdf-file/export-sdf-file.html',
            controller: ExportSdfFileController,
            controllerAs: 'vm',
            bindToController: true,
            link: function($scope, $element, $attr, controllers) {
                $element.addClass('component-button');
                controllers[1].indigoComponents = controllers[0];
            }
        };
    }

    ExportSdfFileController.$inject = ['ProductBatchSummaryOperations', 'batchHelper'];

    function ExportSdfFileController(ProductBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.exportSdfFile = exportSdfFile;
        }

        function exportSdfFile() {
            var exportBatches = vm.isSelectedBatch ? vm.indigoComponents.selectedBatch : vm.indigoComponents.batches;
            ProductBatchSummaryOperations.exportSDFile(exportBatches);
        }
    }
})();
