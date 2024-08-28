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

StructureExportModalController.$inject = ['$uibModalInstance', 'structureToSave', 'structureType', 'FileSaver'];

function StructureExportModalController($uibModalInstance, structureToSave, structureType, FileSaver) {
    var vm = this;
    var NUM_MAX = 999;
    var NUM_MIN = 100;
    var ORDER = 1000;
    var formats = {
        molecule: [{
            name: 'MDL Molfile'
        }],
        reaction: [{
            name: 'RXN File'
        }]
    };

    function init() {
        vm.structureToSave = structureToSave;
        // the only value at this moment
        vm.formats = formats[structureType];
        vm.format = vm.formats[0];
    }

    vm.download = function() {
        var text = vm.structureToSave.replace(/\n/g, '\r\n');
        var isMol = structureType === 'molecule';
        var fileExt = isMol ? 'mol' : 'rxn';

        // to generate file name
        var filename = fileExt + '-' + Math.floor(Math.random(NUM_MIN, NUM_MAX) * ORDER) + '.' + fileExt;
        var data = new Blob([text], {
            type: 'text/plain;charset=utf-8'
        });

        FileSaver.saveAs(data, filename);
        $uibModalInstance.close();
    };

    vm.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };

    init();
}

module.exports = StructureExportModalController;
