(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoComponents', indigoComponents);

    function indigoComponents() {
        IndigoComponentsController.$inject = [
            '$scope',
            'ProductBatchSummaryOperations',
            'ProductBatchSummaryCache',
            'EntitiesBrowser',
            'Principal'
        ];

        return {
            restrict: 'E',
            scope: {
                template: '=',
                isReadonly: '=',
                model: '=',
                experiment: '=',
                saveExperimentFn: '&',
                onChanged: '&'
            },
            templateUrl: 'scripts/indigo-components/components-templete/components.html',
            bindToController: true,
            controllerAs: 'vm',
            controller: IndigoComponentsController
        };

        function IndigoComponentsController($scope, ProductBatchSummaryOperations, ProductBatchSummaryCache,
                                            EntitiesBrowser, Principal) {
            var vm = this;
            var precursors;

            init();

            function init() {
                precursors = '';
                vm.wasOpen = {};
                vm.batches = null;
                vm.batchesTrigger = 0;
                vm.selectedBatch = null;
                vm.selectedBatchTrigger = 0;
                vm.batchOperation = null;
                vm.reactants = null;
                vm.reactantsTrigger = 0;
                vm.activeTabIndex = 0;

                vm.onAddedBatch = onAddedBatch;
                vm.onSelectBatch = onSelectBatch;
                vm.onRemoveBatches = onRemoveBatches;
                vm.onPrecursorsChanged = onPrecursorsChanged;
                vm.onChangedComponent = onChangedComponent;
                vm.setActive = setActive;
                vm.userId = _.get(Principal.getIdentity(), 'id');

                bindEvents();
            }

            function setActive(index) {
                vm.activeTabIndex = index;
                EntitiesBrowser.setExperimentTab(vm.activeTabIndex, vm.experiment.fullId);
            }

            function onChangedComponent(componentId) {
                vm.onChanged({componentId: componentId});
            }

            function updateModel() {
                vm.batches = _.get(vm.model, 'productBatchSummary.batches') || [];
                ProductBatchSummaryCache.setProductBatchSummary(vm.batches);
                vm.batchesTrigger++;

                updateSelections();
            }

            function updateSelections() {
                if ((vm.batches.length && !_.includes(vm.batches, vm.selectedBatch))) {
                    updateSelectedBatch();
                }
            }

            function bindEvents() {
                $scope.$watch('vm.model', updateModel);
                $scope.$watch('vm.experiment', updateActiveTab);

                $scope.$on('stoic-table-recalculated', function(event, data) {
                    if (data.actualProducts.length === vm.batches.length) {
                        _.each(vm.batches, function(batch, i) {
                            batch.theoWeight.value = data.actualProducts[i].theoWeight.value;
                            batch.theoMoles.value = data.actualProducts[i].theoMoles.value;
                        });
                    }
                });
            }

            function updateActiveTab() {
                if (vm.experiment) {
                    EntitiesBrowser.getExperimentTab(vm.experiment.fullId).then(function(index) {
                        vm.activeTabIndex = index || 0;
                    });
                }
            }

            function updateSelectedBatch() {
                var selectedBatch = vm.model && vm.model.productBatchDetails ?
                    _.find(vm.batches, {nbkBatch: _.get(vm.model, 'productBatchDetails.nbkBatch')}) :
                    _.first(vm.batches);

                onSelectBatch(selectedBatch || null);
            }

            function onRemoveBatches(batchesForRemove) {
                var length = vm.batches.length;
                if (needToSelectNew(batchesForRemove)) {
                    onSelectBatch(findPrevBatchToSelect(batchesForRemove));
                }
                ProductBatchSummaryOperations.deleteBatches(vm.batches, batchesForRemove);
                if (vm.batches.length - length) {
                    vm.onChanged();
                    vm.batchesTrigger++;
                }
            }

            function needToSelectNew(batchesForRemove) {
                return vm.selectedBatch && _.includes(batchesForRemove, vm.selectedBatch);
            }

            function findPrevBatchToSelect(batchesForRemove) {
                var batchToSelect;
                var index = _.findIndex(vm.batches, vm.selectedBatch);
                batchToSelect = _.findLast(vm.batches, comparator, index) || _.find(vm.batches, comparator, index);

                return batchToSelect;

                function comparator(batch) {
                    return !_.includes(batchesForRemove, batch);
                }
            }

            function onAddedBatch(batch) {
                batch.precursors = precursors;
                vm.batches.push(batch);
                vm.batchesTrigger++;
            }

            function onSelectBatch(batch) {
                vm.selectedBatch = batch;
                vm.selectedBatchTrigger++;
            }

            function onPrecursorsChanged(newPrecursors) {
                precursors = newPrecursors;
                _.forEach(vm.batches, function(batch) {
                    batch.precursors = precursors;
                });
            }
        }
    }
})();