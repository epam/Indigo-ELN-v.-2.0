angular.module('indigoeln')
    .controller('structureEditorModalController', structureEditorModalController);

/* @ngInject */
function structureEditorModalController($uibModalInstance, prestructure, editor) {
    var vm = this;
    // set attributes
    vm.structure = {
        molfile: prestructure
    };
    vm.editor = editor;

    vm.ok = function() {
        $uibModalInstance.close(vm.structure.molfile);
    };

    vm.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };
}
