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
                isReadonly: '=',
                onChanged: '&'
            },
            controller: indigoBatchStructureController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function indigoBatchStructureController(CalculationService) {
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
                vm.onChanged();
                updateBatchMolInfo();
            }
        }

        function resetMolInfo(row) {
            row.formula = null;
            row.molWeight = null;

            return CalculationService.calculateProductBatch({
                row: row, column: 'totalWeight'
            });
        }

        function updateBatchFormula(batch, molInfo) {
            batch.formula = molInfo.data.molecularFormula;
            batch.molWeight = batch.molWeight || {};
            batch.molWeight.value = molInfo.data.molecularWeight;

            // TODO: it doesn't recalculate stoich table
            CalculationService.recalculateStoich();

            return CalculationService.calculateProductBatch({
                row: batch, column: 'totalWeight'
            });
        }

        function updateBatchMolInfo() {
            if (vm.selectedBatch.structure && vm.selectedBatch.structure.molfile) {
                return CalculationService
                    .getMoleculeInfo(vm.selectedBatch)
                    .then(function(molInfo) {
                        return updateBatchFormula(vm.selectedBatch, molInfo);
                    }, resetMolInfo);
            }

            return resetMolInfo(vm.selectedBatch);
        }
    }
})();
