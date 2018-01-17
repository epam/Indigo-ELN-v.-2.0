require('./indigo-components.less');
var template = require('./indigo-components.html');

function indigoComponents() {
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
        template: template,
        bindToController: true,
        controllerAs: 'vm',
        controller: IndigoComponentsController
    };
}

/* @ngInject */
function IndigoComponentsController($scope, productBatchSummaryOperations, productBatchSummaryCache, calculationHelper,
                                    entitiesBrowserService, principalService, batchHelper, stoichTableHelper) {
    var vm = this;
    var precursors;

    init();

    function init() {
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
        vm.onStoichTableChanged = onStoichTableChanged;
        vm.onChangedComponent = onChangedComponent;
        vm.setActive = setActive;
        vm.userId = _.get(principalService.getIdentity(), 'id');

        bindEvents();
    }

    function getPrecursorsFromStoich() {
        var stoichTable = _.get(vm.experiment, 'components.stoichTable');
        if (!stoichTable) {
            return '';
        }

        return stoichTableHelper.getPrecursors(stoichTable.reactants);
    }

    function setActive(index) {
        vm.activeTabIndex = index;
        entitiesBrowserService.setExperimentTab(vm.activeTabIndex, vm.experiment.fullId);
    }

    function onChangedComponent(componentId) {
        vm.onChanged({componentId: componentId});
    }

    function updateBatches() {
        batchesChanged();
        vm.batchesTrigger++;
    }

    function updateModel() {
        precursors = getPrecursorsFromStoich();
        vm.batches = _.get(vm.model, 'productBatchSummary.batches') || [];
        productBatchSummaryCache.setProductBatchSummary(vm.batches);
        updateBatches();

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

        // $scope.$on('stoic-table-recalculated', function(event, data) {
        //     if (data.actualProducts.length === vm.batches.length) {
        //         _.each(vm.batches, function(batch, i) {
        //             batch.theoWeight.value = data.actualProducts[i].theoWeight.value;
        //             batch.theoMoles.value = data.actualProducts[i].theoMoles.value;
        //         });
        //     }
        // });
    }

    function updateActiveTab() {
        if (vm.experiment) {
            entitiesBrowserService.getExperimentTab(vm.experiment.fullId).then(function(index) {
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
        productBatchSummaryOperations.deleteBatches(vm.batches, batchesForRemove);
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

    function onStoichTableChanged(stoichTable) {
        var limitingRow = calculationHelper.findLimitingRow(stoichTable.reactants);

        var batchesData = {
            rows: vm.batches,
            limitingRow: limitingRow
        };

        batchHelper.calculateAllRows(batchesData);
    }

    function batchesChanged() {
        _.each(vm.batches, function(batch) {
            batch.$$purity = batch.purity ? batch.purity.asString : null;
            batch.$$externalSupplier = batch.externalSupplier ? batch.externalSupplier.asString : null;
            batch.$$meltingPoint = batch.meltingPoint ? batch.meltingPoint.asString : null;
            batch.$$healthHazards = batch.healthHazards ? batch.healthHazards.asString : null;
            batch.$$batchType = getBatchType(batch);
        });
    }

    function getBatchType(batch) {
        if (!batch.batchType) {
            return null;
        }

        return batchHelper.compounds[0].name === batch.batchType ?
            batchHelper.compounds[0]
            : batchHelper.compounds[1];
    }
}

module.exports = indigoComponents;
