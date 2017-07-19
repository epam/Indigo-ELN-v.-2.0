(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

    /* @ngInject */
    function indigoComponent($compile, Components) {
        var components = _.keyBy(Components, 'id');

        var batchAttributes = ' model="vm.model"' +
            ' batches="vm.batches"' +
            ' on-added-batch="vm.ComponentsCtrl.onAddedBatch(batch)"' +
            ' batches-trigger="vm.ComponentsCtrl.batchesTrigger"' +
            ' selected-batch="vm.ComponentsCtrl.selectedBatch"' +
            ' selected-batch-trigger="vm.ComponentsCtrl.selectedBatchTrigger"' +
            ' reactants="vm.ComponentsCtrl.reactants"' +
            ' reactants-trigger="vm.ComponentsCtrl.reactantsTrigger"' +
            ' on-select-batch="vm.ComponentsCtrl.onSelectBatch(batch)"' +
            ' experiment="vm.experiment"' +
            ' readonly="vm.readonly"' +
            ' experiment-form="vm.experimentForm"' +
            ' on-remove-batches="vm.ComponentsCtrl.onRemoveBatches(batches)"' +
            ' indigo-save-experiment-fn="vm.indigoSaveExperimentFn"';

        var defaultAttributes = ' model="vm.model"' +
            ' reactants="vm.ComponentsCtrl.reactants"' +
            ' reactants-trigger="vm.ComponentsCtrl.reactantsTrigger"' +
            ' experiment="vm.experiment"' +
            ' experiment-form="vm.experimentForm"' +
            ' readonly="vm.readonly"';

        return {
            restrict: 'E',
            require: ['indigoComponent', '^indigoComponents'],
            scope: {
                componentId: '@',
                model: '=',
                batches: '=',
                experiment: '=',
                readonly: '=',
                experimentForm: '=',
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
            vm.experiment = _.extend({}, vm.experiment);

            compileTemplate();

            function compileTemplate() {
                var template = getTemplate(vm.componentId);
                $element.append(template);
                $compile(template)($scope);
            }
        }

        /* @ngInject */
        function indigoComponentController() {

        }

        function getTemplate(id) {
            var directiveName = 'indigo-' + id;

            return angular.element('<' + directiveName + getComponentAttributes(id) + '></' + directiveName + '>');
        }

        function getComponentAttributes(id) {
            if (components[id].isBatch) {
                return batchAttributes;
            }

            return defaultAttributes;
        }
    }
})();
