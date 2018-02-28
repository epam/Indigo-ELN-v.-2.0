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

var template = require('./compound-summary.html');

function indigoCompoundSummary() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            batches: '=',
            batchesTrigger: '=',
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            experimentName: '=',
            structureSize: '=',
            isHideColumnSettings: '=',
            isReadonly: '=',
            batchOperation: '=',
            onShowStructure: '&',
            onAddedBatch: '&',
            onSelectBatch: '&',
            onRemoveBatches: '&',
            onChanged: '&'
        },
        bindToController: true,
        controllerAs: 'vm',
        controller: IndigoCompoundSummaryController
    };
}

/* @ngInject */
function IndigoCompoundSummaryController($scope, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.columns = getDefaultColumns();
        vm.hasCheckedRows = batchHelper.hasCheckedRow;
        vm.vnv = angular.noop;
        vm.registerVC = angular.noop;
        vm.onBatchChanged = batchHelper.onBatchChanged;
        vm.onChangedVisibleColumn = onChangedVisibleColumn;

        bindEvents();
    }

    function onChangedVisibleColumn(column, isVisible) {
        if (column.id === 'structure') {
            vm.onShowStructure({isVisible: isVisible});
        }
    }

    function getDefaultColumns() {
        return [
            batchHelper.columns.structure,
            batchHelper.columns.nbkBatch,
            batchHelper.columns.select,
            batchHelper.columns.virtualCompoundId,
            batchHelper.columns.molWeight,
            batchHelper.columns.formula,
            batchHelper.columns.stereoisomer,
            batchHelper.columns.structureComments,
            batchHelper.columns.saltCode,
            batchHelper.columns.saltEq
        ];
    }

    function bindEvents() {
        $scope.$watch('vm.structureSize', function(newVal) {
            var column = _.find(vm.columns, function(item) {
                return item.id === 'structure';
            });
            if (column) {
                column.width = (500 * newVal) + 'px';
            }
        });
    }
}

module.exports = indigoCompoundSummary;
