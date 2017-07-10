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
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                share: '=',
                experiment: '=',
                indigoReadonly: '=readonly'
            }
        };

        /* @ngInject */
        function indigoPreferredCompoundDetailsController($scope, EntitiesBrowser) {
            var vm = this;

            vm.experiment = vm.experiment || {};
            vm.share = vm.share || {};
            vm.model = vm.model || {};
            vm.model.preferCompoundDetails = vm.model.preferCompoundDetails || {};
            vm.model.preferredCompoundSummary = vm.model.preferredCompoundSummary || {};
            vm.showStructure = false;
            vm.showSummary = false;
            vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
            vm.selectControl = {};

            vm.onSelectCompound = onSelectCompound;

            init();

            function init() {
                $scope.$on('product-batch-structure-changed', updateStructureImage);
                $scope.$watch('vm.selectedBatchTrigger', onSelectedBatchChanged);
            }

            function onSelectedBatchChanged() {
                if (vm.selectedBatch) {
                    selectCompound(vm.selectedBatch);
                } else {
                    deselectCompound();
                }
            }

            function updateStructureImage(event, data) {
                if (data.structure) {
                    vm.structureImage = data.structure.image;
                } else {
                    vm.structureImage = '';
                }
            }

            function onSelectCompound() {
                selectCompound(vm.share.selectedRow);
            }

            function selectCompound(batch) {
                vm.model.preferCompoundDetails = batch;

                if (batch.structure) {
                    vm.structureImage = batch.structure.image;
                } else {
                    vm.structureImage = '';
                }

                vm.selectedCompound = batch;
                vm.selectControl.setSelection(batch);
            }

            function deselectCompound() {
                vm.model.preferCompoundDetails = {};
                vm.structureImage = '';
                vm.selectControl.unSelect();
            }
        }
    }
})();
