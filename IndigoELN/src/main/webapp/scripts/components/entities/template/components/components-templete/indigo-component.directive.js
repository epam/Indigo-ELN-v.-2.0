(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

    /* @ngInject */
    function indigoComponent($compile, Components) {
        var components = _.keyBy(Components, 'id');

        var defaultAttributes = ' model="vm.model"' +
            ' reactants="vm.ComponentsCtrl.reactants"' +
            ' reactants-trigger="vm.ComponentsCtrl.reactantsTrigger"' +
            ' experiment="vm.experiment"' +
            ' experiment-form="vm.experimentForm"' +
            ' readonly="vm.readonly"';

        var batchAttributes = defaultAttributes +
            ' batches="vm.batches"' +
            ' on-added-batch="vm.ComponentsCtrl.onAddedBatch(batch)"' +
            ' batches-trigger="vm.ComponentsCtrl.batchesTrigger"' +
            ' selected-batch="vm.ComponentsCtrl.selectedBatch"' +
            ' selected-batch-trigger="vm.ComponentsCtrl.selectedBatchTrigger"' +
            ' on-select-batch="vm.ComponentsCtrl.onSelectBatch(batch)"' +
            ' on-remove-batches="vm.ComponentsCtrl.onRemoveBatches(batches)"' +
            ' save-experiment-fn="vm.saveExperimentFn()"';

        var stoichTableAttributes = defaultAttributes +
            ' on-precursors-changed="vm.ComponentsCtrl.onPrecursorsChanged(precursors)"';

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
                saveExperimentFn: '&'
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
