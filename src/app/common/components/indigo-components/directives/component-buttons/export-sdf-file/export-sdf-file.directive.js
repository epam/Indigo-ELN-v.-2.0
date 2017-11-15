/* eslint no-shadow: "off"*/
var template = require('./export-sdf-file.html');

function exportSdfFile() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'exportSdfFile'],
        scope: {
            isReadonly: '=',
            isSelectedBatch: '=?'
        },
        template: template,
        controller: ExportSdfFileController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };

    /* @ngInject */
    function ExportSdfFileController(productBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.exportSdfFile = exportSdfFile;
        }

        function exportSdfFile() {
            var exportBatches = vm.isSelectedBatch ? vm.indigoComponents.selectedBatch : vm.indigoComponents.batches;
            productBatchSummaryOperations.exportSDFile(exportBatches);
        }
    }
}

module.exports = exportSdfFile;
