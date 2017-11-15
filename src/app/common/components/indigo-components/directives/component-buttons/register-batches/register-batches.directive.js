var template = require('./register-batches.html');

function registerBatchesDirective() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'registerBatches'],
        scope: {
            isReadonly: '='
        },
        template: template,
        controller: RegisterBatchesController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };
}

RegisterBatchesController.$inject = ['productBatchSummaryOperations', 'batchHelperService',
    'notifyService', 'alertModalService', 'registrationUtilService'];

function RegisterBatchesController(productBatchSummaryOperations, batchHelperService,
                                   notifyService, alertModalService, registrationUtilService) {
    var vm = this;

    init();

    function init() {
        registrationUtilService.hasRegistrationService().then(function(hasRegService) {
            vm.hasRegService = hasRegService;
        });

        vm.registerBatches = registerBatches;
    }

    function registerBatches() {
        var batches = batchHelperService.getCheckedBatches(vm.indigoComponents.batches);
        var notFullBatches = registrationUtilService.getNotFullForRegistrationBatches(batches);

        if (notFullBatches.length) {
            return showError(notFullBatches);
        }

        if (checkNonEditableBatches(batches)) {
            vm.indigoComponents.batchOperation =
                vm.indigoComponents.saveExperimentFn()
                    .then(function() {
                        return productBatchSummaryOperations.registerBatches(batches);
                    });
        }
    }

    function checkNonEditableBatches(batches) {
        var nonEditableBatches = productBatchSummaryOperations.getSelectedNonEditableBatches(batches);
        if (!_.isEmpty(nonEditableBatches)) {
            notifyService.warning('Batch(es) ' + _.uniq(nonEditableBatches)
                .join(', ') + ' already have been registered.');

            return false;
        }

        return true;
    }

    function showError(batches) {
        var message = '';
        _.forEach(batches, function(notFullBatch) {
            message = message + '<br><b>Batch '
                + notFullBatch.nbkBatch + ':</b><br>' + notFullBatch.emptyFields.join('<br>');
        });
        alertModalService.error(message);
    }
}

module.exports = registerBatchesDirective;
