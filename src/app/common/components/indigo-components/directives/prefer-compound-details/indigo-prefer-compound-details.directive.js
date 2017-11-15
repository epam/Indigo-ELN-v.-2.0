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
            isReadonly: '=',
            batchOperation: '=',
            onSelectBatch: '&',
            onAddedBatch: '&',
            onRemoveBatches: '&',
            structureSize: '=',
            isHideColumnSettings: '=',
            isExistStoichTable: '=',
            onChanged: '&'
        }
    };
}

IndigoPreferredCompoundDetailsController.$inject = ['$scope', 'entitiesBrowserService',
    'appValuesService', 'batchHelperService'];

function IndigoPreferredCompoundDetailsController($scope, entitiesBrowserService, appValuesService,
                                                  batchHelperService) {
    var vm = this;

    init();

    function init() {
        vm.showSummary = false;
        vm.notebookId = entitiesBrowserService.getActiveTab().$$title;
        vm.saltCodeValues = appValuesService.getSaltCodeValues();
        vm.selectControl = {};
        vm.hasCheckedRows = batchHelperService.hasCheckedRow;
        vm.selectBatch = selectBatch;
        vm.canEditSaltEq = batchHelperService.canEditSaltEq;
        vm.recalculateSalt = batchHelperService.recalculateSalt;
        vm.onBatchOperationChanged = onBatchOperationChanged;
        vm.isBatchLoading = false;

        bindEvents();
    }

    function selectBatch(batch) {
        vm.onSelectBatch({batch: batch});
    }

    function checkEditDisabled() {
        return vm.isReadonly
            || !vm.selectedBatch
            || !vm.selectedBatch.nbkBatch
            || !!vm.selectedBatch.registrationStatus;
    }

    function onBatchOperationChanged(completed) {
        vm.isBatchLoading = !completed;
    }

    function bindEvents() {
        $scope.$watch(checkEditDisabled, function(newValue) {
            vm.isEditDisabled = newValue;
        });
    }
}

module.exports = indigoPreferredCompoundDetails;

