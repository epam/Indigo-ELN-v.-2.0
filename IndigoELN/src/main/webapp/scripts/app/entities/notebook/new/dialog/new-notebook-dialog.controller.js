'use strict';

angular.module('indigoeln')
    .controller('NewNotebookDialogController', function ($uibModalInstance) {
        var vm = this;
        vm.notebookName = "";
        vm.ok = okPressed;
        vm.cancel = cancelPressed;

        function okPressed () {
            $uibModalInstance.close(vm.notebookName);
        }

        function cancelPressed () {
            $uibModalInstance.dismiss();
        }
    });
