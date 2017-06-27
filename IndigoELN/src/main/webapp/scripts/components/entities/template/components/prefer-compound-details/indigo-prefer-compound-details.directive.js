(function() {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundDetails', indigoPreferredCompoundDetails);

    function indigoPreferredCompoundDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/prefer-compound-details/prefer-compound-details.html',
            controller: controller,
            controllerAs: 'vm'
        };

        /* @ngInject */
        function controller($scope, EntitiesBrowser) {
            var vm = this;

            vm.indigoReadonly = $scope.indigoReadonly;
            vm.experiment = $scope.experiment || {};
            vm.share = $scope.share || {};
            vm.model = $scope.model || {};
            vm.model.preferCompoundDetails = vm.model.preferCompoundDetails || {};
            vm.model.preferredCompoundSummary = vm.model.preferredCompoundSummary || {};
            vm.showStructure = false;
            vm.showSummary = false;
            vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
            vm.selectControl = {};

            vm.onSelectCompound = onSelectCompound;

            init();

            function init() {
                var onCompoundSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function(event, data) {
                    selectCompound(data.row);
                });

                var onCompoundStructureChanged = $scope.$on('product-batch-structure-changed', function(event, data) {
                    if (data.structure) {
                        vm.structureImage = data.structure.image;
                    } else {
                        vm.structureImage = '';
                    }
                });

                var onCompoundSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', function() {
                    deselectCompound();
                });

                $scope.$on('$destroy', function() {
                    onCompoundSummaryRowSelectedEvent();
                    onCompoundSummaryRowDeselectedEvent();
                    onCompoundStructureChanged();
                });
            }

            function onSelectCompound() {
                if (vm.share.selectedRow) {
                    vm.share.selectedRow.$$selected = false;
                }
                vm.share.selectedRow = $scope.selectedCompound || {};
                vm.share.selectedRow.$$selected = true;
                selectCompound(vm.share.selectedRow);
            }

            function selectCompound(row) {
                vm.model.preferCompoundDetails = row;

                if (row.structure) {
                    vm.structureImage = row.structure.image;
                } else {
                    vm.structureImage = '';
                }

                vm.selectedCompound = row;
                vm.selectControl.setSelection(row);
            }

            function deselectCompound() {
                vm.model.preferCompoundDetails = {};
                vm.structureImage = '';
                vm.selectControl.unSelect();
            }
        }
    }
})();