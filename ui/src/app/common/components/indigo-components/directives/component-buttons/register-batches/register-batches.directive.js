/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var template = require('./register-batches.html');

function registerBatchesDirective() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'registerBatches'],
        scope: {
            isReadonly: '=',
            isDetails: '='
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

RegisterBatchesController.$inject = ['productBatchSummaryOperations', 'batchHelper',
    'notifyService', 'alertModal', 'registrationUtil'];

function RegisterBatchesController(productBatchSummaryOperations, batchHelper,
                                   notifyService, alertModal, registrationUtil) {
    var vm = this;

    init();

    function init() {
        registrationUtil.hasRegistrationService().then(function(hasRegService) {
            vm.hasRegService = hasRegService;
        });

        vm.registerBatches = registerBatches;
    }

    function registerBatches() {
        var batches = vm.isDetails
            ? [vm.indigoComponents.selectedBatch]
            : batchHelper.getCheckedBatches(vm.indigoComponents.batches);
        var notFullBatches = registrationUtil.getNotFullForRegistrationBatches(batches);

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
        alertModal.error(message);
    }
}

module.exports = registerBatchesDirective;
