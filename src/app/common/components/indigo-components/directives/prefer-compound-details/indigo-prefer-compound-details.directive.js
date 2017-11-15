var template = require('./prefer-compound-details.html');

function indigoPreferredCompoundDetails() {
    return {
        restrict: 'E',
        replace: true,
        template: template,
        controller: IndigoPreferredCompoundDetailsController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            componentData: '=',
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
            isExistStoichTable: '=',
            onChanged: '&'
        }
    };
}

IndigoPreferredCompoundDetailsController.$inject = ['$scope', 'entitiesBrowserService',
    'appValuesService', 'batchHelper'];

function IndigoPreferredCompoundDetailsController($scope, entitiesBrowserService,
                                                  appValuesService, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.experiment = vm.experiment || {};
        vm.showStructure = false;
        vm.showSummary = false;
        vm.notebookId = entitiesBrowserService.getActiveTab().$$title;
        vm.saltCodeValues = appValuesService.getSaltCodeValues();
        vm.selectControl = {};
        vm.hasCheckedRows = batchHelper.hasCheckedRow;
        vm.selectBatch = selectBatch;
        vm.canEditSaltEq = canEditSaltEq;
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
        return vm.isReadonly || !vm.selectedBatch || !vm.selectedBatch.nbkBatch ||
            vm.selectedBatch.registrationStatus;
    }

    function onBatchOperationChanged(completed) {
        vm.isBatchLoading = !completed;
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
    }
}

module.exports = indigoPreferredCompoundDetails;

