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

/* eslint no-shadow: "off"*/
var template = require('./import-sdf-file.html');

function importSdfFile() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'importSdfFile'],
        scope: {
            isReadonly: '='
        },
        template: template,
        controller: ImportSdfFileController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };

    /* @ngInject */
    function ImportSdfFileController(productBatchSummaryOperations, notifyService, $q, $scope) {
        var vm = this;

        init();

        function init() {
            vm.importSdfFile = importSdfFile;

            bindEvents();
        }

        function importSdfFile() {
            vm.indigoComponents.batchOperation = productBatchSummaryOperations
                .importSDFile(vm.indigoComponents.experiment)
                .then(function(batches) {
                    if (batches.length > 0) {
                        notifyService.info(batches.length + ' batches successfully imported');

                        return batches;
                    }

                    return $q.reject('Error! Batch(es) not imported');
                })
                .then(successAddedBatches)
                .catch(function(e) {
                    if (e) {
                        notifyService.error(e);
                    }
                });
        }

        function successAddedBatches(batches) {
            if (batches.length) {
                _.forEach(batches, function(batch) {
                    vm.indigoComponents.onAddedBatch(batch);
                });
                vm.indigoComponents.onSelectBatch(_.last(batches));
            }
        }

        function bindEvents() {
            $scope.$on('$destroy', function() {
                productBatchSummaryOperations.closeImportSDFileDialog();
            });
        }
    }
}

module.exports = importSdfFile;
