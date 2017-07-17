(function() {
    angular
        .module('indigoeln')
        .directive('indigoBatchStructure', indigoBatchStructure);

    function indigoBatchStructure() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/batch-structure/batch-structure.html',
            scope: {
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                readonly: '='
            },
            controller: indigoBatchStructureController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function indigoBatchStructureController(EntitiesBrowser, CalculationService) {
        var vm = this;

        init();

        function init() {
            vm.onChangedStructure = onChangedStructure;
            bindEvents();
        }

        function bindEvents() {
        }

        function onChangedStructure(structure) {
            if (vm.selectedBatch) {
                vm.selectedBatch.structure = structure;
                EntitiesBrowser.setCurrentFormDirty();
                updateBatchMolInfo();
            }
        }

        function resetMolInfo(row) {
            row.formula = null;
            row.molWeight = null;
            CalculationService.calculateProductBatch({
                row: row, column: 'totalWeight'
            });
        }

        function getInfoCallback(batch, molInfo) {
            batch.formula = molInfo.data.molecularFormula;
            batch.molWeight = batch.molWeight || {};
            batch.molWeight.value = molInfo.data.molecularWeight;

            // TODO: it doesn't recalculate stoich table
            CalculationService.recalculateStoich();

            CalculationService.calculateProductBatch({
                row: batch, column: 'totalWeight'
            });
        }

        function updateBatchMolInfo() {
            if (vm.selectedBatch.structure && vm.selectedBatch.structure.molfile) {
                CalculationService.getMoleculeInfo(vm.selectedBatch, function(molInfo) {
                    getInfoCallback(vm.selectedBatch, molInfo);
                }, resetMolInfo);

                return;
            }

            resetMolInfo(vm.selectedBatch);
        }
    }
})();
