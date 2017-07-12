(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

    /* @ngInject */
    function indigoComponent($compile) {
        return {
            restrict: 'E',
            require: ['indigoComponent', '^indigoComponents'],
            scope: {
                componentId: '@',
                model: '=',
                batches: '=',
                indigoExperiment: '=',
                readonly: '=',
                indigoExperimentForm: '=',
                share: '=',
                indigoSaveExperimentFn: '&'
            },
            link: link,
            controller: indigoComponentController,
            controllerAs: 'vm',
            bindToController: true
        };

        function link($scope, $element, $attr, controllers) {
            var vm = controllers[0];

            vm.ComponentsCtrl = controllers[1];

            // for readonly
            vm.experiment = _.extend({}, vm.indigoExperiment);

            compileTemplate();

            function compileTemplate() {
                var id = vm.componentId;
                var compileElement = getTemplate(id);
                $element.append(compileElement);
                $compile(compileElement)($scope);
            }
        }

        /* @ngInject */
        function indigoComponentController($rootScope, $scope) {
            var vm = this;

            init();

            function init() {
                // vm.selectedBatch = null;
                // vm.selectedBatchTrigger = 0;

                // vm.compoundSummarySelectedRow = compoundSummarySelectedRow;
                // vm.onSelectBatch = onSelectBatch;

                bindEvents();
            }

            function bindEvents() {
            }
        }

        function getTemplate(id) {
            var directiveName = 'indigo-' + id;

            return angular.element('<' + directiveName +
                ' model="vm.model"' +
                ' batches="vm.batches"' +
                ' selected-batch="vm.ComponentsCtrl.selectedBatch"' +
                ' selected-batch-trigger="vm.ComponentsCtrl.selectedBatchTrigger"' +
                ' on-select-batch="vm.ComponentsCtrl.onSelectBatch(batch)"' +
                ' experiment="vm.experiment"' +
                ' share="vm.share"' +
                ' readonly="vm.readonly"' +
                ' experiment-form="vm.indigoExperimentForm"' +
                ' indigo-share="vm.share"' +
                ' on-remove-batches="vm.ComponentsCtrl.onRemoveBatches(batches)"' +
                ' indigo-save-experiment-fn="vm.indigoSaveExperimentFn">' +
                '</' + directiveName + '>');
        }
    }
})();
