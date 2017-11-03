(function() {
    angular
        .module('indigoeln')
        .controller('UserManagementDeleteController', UserManagementDeleteController);

    /* @ngInject */
    function UserManagementDeleteController($uibModalInstance, entity, userService) {
        var vm = this;

        vm.user = entity;

        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete(login) {
            userService.delete({
                login: login
            },
                function() {
                    $uibModalInstance.close(true);
                },
                function() {
                    $uibModalInstance.close(false);
                });
        }
    }
})();
