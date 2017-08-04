(function() {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundDetails', indigoPreferredCompoundDetails);

    function indigoPreferredCompoundDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/prefer-compound-details/prefer-compound-details.html',
            controller: indigoPreferredCompoundDetailsController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                batches: '=',
                batchesTrigger: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                experiment: '=',
                isReadonly: '=',
                batchOperation: '=',
                onSelectBatch: '&',
                onAddedBatch: '&',
                onRemoveBatches: '&',
                experimentName: '=',
                structureSize: '=',
                isHideColumnSettings: '='
            }
        };

        /* @ngInject */
        function indigoPreferredCompoundDetailsController($scope, EntitiesBrowser) {
            var vm = this;

            init();

            function init() {
                vm.experiment = vm.experiment || {};
                vm.model = vm.model || {};
                vm.model.preferCompoundDetails = vm.model.preferCompoundDetails || {};
                vm.model.preferredCompoundSummary = vm.model.preferredCompoundSummary || {};
                vm.showStructure = false;
                vm.showSummary = false;
                vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
                vm.selectControl = {};

                vm.onSelectCompound = onSelectBatch;
                vm.addNewCompound = addNewCompound;

                bindEvents();
            }

            function bindEvents() {
                $scope.$watch('vm.selectedBatchTrigger', onSelectedBatchChanged);
            }

            function addNewCompound() {

            }

            function onSelectedBatchChanged() {
                if (vm.selectedBatch) {
                    selectCompound(vm.selectedBatch);
                } else {
                    deselectCompound();
                }
            }

            function onSelectBatch(compound) {
                vm.onSelectBatch({batch: compound});
                selectCompound(compound);
            }

            function selectCompound(compound) {
                vm.model.preferCompoundDetails = compound;

                vm.selectedCompound = compound;
                vm.selectControl.setSelection(compound);
            }

            function deselectCompound() {
                vm.model.preferCompoundDetails = {};
                vm.selectControl.unSelect();
            }
        }
    }
})();
