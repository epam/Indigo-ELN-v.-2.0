angular
    .module('indigoeln')
    .controller('structureExportModalController', structureExportModalController);

function structureExportModalController($uibModalInstance, structureToSave, structureType, FileSaver) {
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
