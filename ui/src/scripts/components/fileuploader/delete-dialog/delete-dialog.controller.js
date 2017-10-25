(function() {
    angular
        .module('indigoeln')
        .controller('FileUploaderDeleteDialogController', FileUploaderDeleteDialogController);

    /* @ngInject */
    function FileUploaderDeleteDialogController($uibModalInstance, notifyService, UploaderService, params, file) {
        var vm = this;

        vm.deleteFile = deleteFile;
        vm.clear = clear;

        function deleteFile() {
            if (params.projectId) {
                UploaderService
                    .delete({
                        id: file.id
                    })
                    .$promise.then(
                    function() {
                        $uibModalInstance.close(file);
                    },
                    function(error) {
                        notifyService.error('Error deleting the file: ' + error);
                        $uibModalInstance.close();
                    }
                );
            } else {
                $uibModalInstance.close(file);
            }
        }

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
