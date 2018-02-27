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

var template = require('./batch-structure.html');
var fieldTypes = require('../../services/calculation/field-types');

function indigoBatchStructure() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            selectedBatch: '=',
            selectedBatchTrigger: '=',
            isReadonly: '=',
            onChanged: '&'
        },
        controller: IndigoBatchStructureController,
        controllerAs: 'vm',
        bindToController: true
    };
}

/* @ngInject */
function IndigoBatchStructureController($q, calculationService, batchHelper) {
    var vm = this;

    init();

    function init() {
        vm.onChangedStructure = onChangedStructure;
        bindEvents();
    }

    function bindEvents() {
    }

    function onChangedStructure(structure) {
        if (vm.selectedBatch) {
            vm.selectedBatch.structure = structure;
            vm.onChanged();
            updateBatchMolInfo();
        }
    }

    function getColumn(batch) {
        return batch.totalWeight.entered ? 'totalWeight' : 'mol';
    }

    function resetMolInfo(batch) {
        batch.formula = null;
        batch.molWeight = null;

        return batchHelper.calculateRow({changedRow: batch, changedField: getColumn(batch)});
    }

    function updateBatchFormula(batch, molInfo) {
        batch.formula.value = molInfo.molecularFormula;
        batch.formula.baseValue = molInfo.molecularFormula;
        batch.molWeight.value = molInfo.molecularWeight;
        batch.molWeight.baseValue = molInfo.molecularWeight;

        batchHelper.calculateRow({changedRow: batch, changedField: fieldTypes.molWeight});

        return $q.resolve();
    }

    function updateBatchMolInfo() {
        if (vm.selectedBatch.structure && vm.selectedBatch.structure.molfile) {
            return calculationService
                .getMoleculeInfo(vm.selectedBatch)
                .then(function(molInfo) {
                    return updateBatchFormula(vm.selectedBatch, molInfo);
                }, resetMolInfo);
        }

        return resetMolInfo(vm.selectedBatch);
    }
}

module.exports = indigoBatchStructure;
