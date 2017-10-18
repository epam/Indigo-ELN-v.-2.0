(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoPreferredCompoundDetails', indigoPreferredCompoundDetails);

    function indigoPreferredCompoundDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/indigo-components/prefer-compound-details/prefer-compound-details.html',
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
                isHideColumnSettings: '=',
                onChanged: '&'
            }
        };

        /* @ngInject */
        function indigoPreferredCompoundDetailsController($scope, EntitiesBrowser, ProductBatchSummaryOperations, AppValues) {
            var vm = this;

            init();

            function init() {
                vm.experiment = vm.experiment || {};
                vm.model = vm.model || {};
                vm.showStructure = false;
                vm.showSummary = false;
                vm.notebookId = EntitiesBrowser.getActiveTab().$$title;
                vm.saltCodeValues = AppValues.getSaltCodeValues();
                vm.selectControl = {};
                vm.importSDFile = importSDFile;
                vm.exportSDFile = exportSDFile;

                vm.selectBatch = selectBatch;
                vm.canEditSaltEq = canEditSaltEq;
                vm.deleteBatch = deleteBatch;
                vm.duplicateBatch = duplicateBatch;
                vm.onBatchOperationChanged = onBatchOperationChanged;
                vm.isBatchLoading = false;

                bindEvents();
            }

            function canEditSaltEq() {
                var o = vm.selectedBatch;
                return o && o.saltCode && o.saltCode.value !== 0;
            }

            function selectBatch(batch) {
                vm.onSelectBatch({batch: batch});
            }

            function checkEditDisabled() {
                return vm.isReadonly || !vm.selectedBatch || !vm.selectedBatch.nbkBatch || vm.selectedBatch.registrationStatus;
            }

            function successAddedBatch(batch) {
                vm.onAddedBatch({batch: batch});
                vm.onChanged();
                selectBatch(batch);
            }

            function duplicateBatch() {
                vm.batchOperation = ProductBatchSummaryOperations.duplicateBatch(vm.selectedBatch).then(successAddedBatch);
            }

            function deleteBatch() {
                vm.onRemoveBatches({batches: [vm.selectedBatch]});
            }

            function importSDFile() {
                vm.batchOperation = ProductBatchSummaryOperations.importSDFile().then(function(batches) {
                    _.forEach(batches, function(batch) {
                        vm.onAddedBatch({batch: batch});
                    });
                    selectBatch(batches[0]);
                });
            }

            function exportSDFile() {
                ProductBatchSummaryOperations.exportSDFile();
            }

            function onBatchOperationChanged(completed) {
                vm.isBatchLoading = completed;
            }


            function onRowSelected(batch) {
                if (batch) {
                    vm.selectControl.setSelection(batch);
                } else {
                    vm.selectControl.unSelect();
                }
            }

            function bindEvents() {
                $scope.$watch('vm.selectedBatchTrigger', function() {
                    onRowSelected(vm.selectedBatch);
                });

                $scope.$watch(checkEditDisabled, function(newValue) {
                    vm.isEditDisabled = newValue;
                });

                $scope.$watch('vm.model.stoichTable', function() {
                    vm.isExistStoichTable = !!vm.model.stoichTable;
                });
            }
        }
    }
})();
