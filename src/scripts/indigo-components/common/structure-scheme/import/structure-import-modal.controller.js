angular
    .module('indigoeln.componentsModule')
    .controller('StructureImportModalController', StructureImportModalController);

/* @ngInject */
function StructureImportModalController($uibModalInstance, $timeout) {
    var vm = this;

    $onInit();

    function $onInit() {
        vm.content = null;

        vm.fileContentChanged = fileContentChanged;
        vm.importContent = importContent;
        vm.cancel = cancel;
    }

    function fileContentChanged(fileContent) {
        $timeout(function() {
            vm.content = fileContent;
        });
    }
    function importContent() {
        $uibModalInstance.close(vm.content);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}
