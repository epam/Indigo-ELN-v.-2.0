(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

    /* @ngInject */
    function indigoComponent($compile, Components) {
        var defaultAttributes = ' model="vm.ComponentsCtrl.model"' +
            ' reactants="vm.ComponentsCtrl.reactants"' +
            ' reactants-trigger="vm.ComponentsCtrl.reactantsTrigger"' +
            ' experiment="vm.ComponentsCtrl.experiment"' +
            ' experiment-form="vm.ComponentsCtrl.experimentForm"' +
            ' is-readonly="vm.ComponentsCtrl.isReadonly"' +
            ' on-changed="vm.ComponentsCtrl.onChangedComponent({componentId: vm.component.id})"';

        var batchAttributes = defaultAttributes +
            ' batches="vm.ComponentsCtrl.batches"' +
            ' on-added-batch="vm.ComponentsCtrl.onAddedBatch(batch)"' +
            ' batches-trigger="vm.ComponentsCtrl.batchesTrigger"' +
            ' selected-batch="vm.ComponentsCtrl.selectedBatch"' +
            ' selected-batch-trigger="vm.ComponentsCtrl.selectedBatchTrigger"' +
            ' on-select-batch="vm.ComponentsCtrl.onSelectBatch(batch)"' +
            ' on-remove-batches="vm.ComponentsCtrl.onRemoveBatches(batches)"' +
            ' batch-operation="vm.ComponentsCtrl.batchOperation"' +
            ' save-experiment-fn="vm.ComponentsCtrl.saveExperimentFn()"';

        var stoichTableAttributes = defaultAttributes +
            ' on-precursors-changed="vm.ComponentsCtrl.onPrecursorsChanged(precursors)"';

        return {
            restrict: 'E',
            require: ['indigoComponent', '^indigoComponents'],
            scope: {
                component: '='
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
                var template = getTemplate(vm.component.id);
                $element.append(template);
                $compile(template)($scope);
            }

            function getTemplate(id) {
                var directiveName = 'indigo-' + id;

                return angular.element('<' + directiveName + getComponentAttributes(id) + '></' + directiveName + '>');
            }

            function getComponentAttributes(id) {
                var component = _.find(Components, {id: id});
                if (component.isBatch) {
                    return batchAttributes;
                }
                if (id === 'stoich-table') {
                    return stoichTableAttributes;
                }

                return defaultAttributes;
            }
        }

        /* @ngInject */
        function indigoComponentController() {

        }
    }
})();
