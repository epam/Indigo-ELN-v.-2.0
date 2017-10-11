(function() {
    angular
        .module('indigoeln')
        .run(function($templateCache, Components) {
            var defaultAttributes = ' model="vm.ComponentsCtrl.model"' +
                ' reactants="vm.ComponentsCtrl.reactants"' +
                ' reactants-trigger="vm.ComponentsCtrl.reactantsTrigger"' +
                ' experiment="vm.ComponentsCtrl.experiment"' +
                ' is-readonly="vm.ComponentsCtrl.isReadonly"' +
                ' on-changed="vm.ComponentsCtrl.onChangedComponent({componentId: component.id})"';

            var batchAttributes = defaultAttributes +
                ' batches="vm.ComponentsCtrl.batches"' +
                ' on-added-batch="vm.ComponentsCtrl.onAddedBatch(batch)"' +
                ' batches-trigger="vm.ComponentsCtrl.batchesTrigger"' +
                ' selected-batch="vm.ComponentsCtrl.selectedBatch"' +
                ' is-exist-stoich-table="::!!vm.ComponentsCtrl.model.stoichTable"' +
                ' selected-batch-trigger="vm.ComponentsCtrl.selectedBatchTrigger"' +
                ' on-select-batch="vm.ComponentsCtrl.onSelectBatch(batch)"' +
                ' on-remove-batches="vm.ComponentsCtrl.onRemoveBatches(batches)"' +
                ' batch-operation="vm.ComponentsCtrl.batchOperation"' +
                ' save-experiment-fn="vm.ComponentsCtrl.saveExperimentFn()"';

            var stoichTableAttributes = defaultAttributes +
                ' on-precursors-changed="vm.ComponentsCtrl.onPrecursorsChanged(precursors)"' +
                ' info-reactants="vm.ComponentsCtrl.model.reaction.infoReactants"' +
                ' info-products="vm.ComponentsCtrl.model.reaction.infoProducts"';

            _.forEach(Components, function(component) {
                $templateCache.put(component.id, getTemplate(component.id));
            });

            function getTemplate(id) {
                var directiveName = 'indigo-' + id;

                return '<' + directiveName + getComponentAttributes(id) + '></' + directiveName + '>';
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
        })
        .directive('indigoComponent', indigoComponent);

    function indigoComponent() {
        return {
            restrict: 'E',
            require: ['indigoComponent', '^indigoComponents'],
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true,
            template: '<data-ng-include src="component.id"></data-ng-include>',
            link: {
                pre: function($scope, $element, $attr, controllers) {
                    var vm = controllers[0];

                    vm.ComponentsCtrl = controllers[1];
                }
            }
        };

    }
})();
