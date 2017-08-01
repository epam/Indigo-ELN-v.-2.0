(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

    /* @ngInject */
    function indigoComponent($compile, Components) {
        var components = _.keyBy(Components, 'id');

        var defaultAttributes = ' model="vm.ComponentsCtrl.model"' +
            ' reactants="vm.ComponentsCtrl.reactants"' +
            ' reactants-trigger="vm.ComponentsCtrl.reactantsTrigger"' +
            ' experiment="vm.ComponentsCtrl.experiment"' +
            ' experiment-form="vm.ComponentsCtrl.experimentForm"' +
            ' is-readonly="vm.ComponentsCtrl.isReadonly"';

        var batchAttributes = defaultAttributes +
            ' batches="vm.ComponentsCtrl.batches"' +
            ' on-added-batch="vm.ComponentsCtrl.onAddedBatch(batch)"' +
            ' batches-trigger="vm.ComponentsCtrl.batchesTrigger"' +
            ' selected-batch="vm.ComponentsCtrl.selectedBatch"' +
            ' selected-batch-trigger="vm.ComponentsCtrl.selectedBatchTrigger"' +
            ' on-select-batch="vm.ComponentsCtrl.onSelectBatch(batch)"' +
            ' on-remove-batches="vm.ComponentsCtrl.onRemoveBatches(batches)"' +
            ' is-batches-locked="vm.ComponentsCtrl.isBatchesLocked"' +
            ' save-experiment-fn="vm.ComponentsCtrl.saveExperimentFn()"';

        var stoichTableAttributes = defaultAttributes +
            ' on-precursors-changed="vm.ComponentsCtrl.onPrecursorsChanged(precursors)"';

        return {
            restrict: 'E',
            require: ['indigoComponent', '^indigoComponents'],
            scope: {
                componentId: '@'
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
            if (id === 'stoich-table') {
                return stoichTableAttributes;
            }

            return defaultAttributes;
        }
    }
})();
