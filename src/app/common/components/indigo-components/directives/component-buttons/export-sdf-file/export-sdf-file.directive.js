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
var template = require('./export-sdf-file.html');

function exportSdfFile() {
    return {
        restrict: 'E',
        require: ['^^indigoComponents', 'exportSdfFile'],
        scope: {
            isReadonly: '=',
            isSelectedBatch: '=?'
        },
        template: template,
        controller: ExportSdfFileController,
        controllerAs: 'vm',
        bindToController: true,
        link: function($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
            controllers[1].indigoComponents = controllers[0];
        }
    };

    /* @ngInject */
    function ExportSdfFileController(productBatchSummaryOperations) {
        var vm = this;

        init();

        function init() {
            vm.exportSdfFile = exportSdfFile;
        }

        function exportSdfFile() {
            var exportBatches = vm.isSelectedBatch ? vm.indigoComponents.selectedBatch : vm.indigoComponents.batches;
            productBatchSummaryOperations.exportSDFile(exportBatches);
        }
    }
}

module.exports = exportSdfFile;
