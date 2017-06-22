(function () {
    angular
        .module('indigoeln')
        .controller('RoleManagementDeleteController', RoleManagementDeleteController);

    /* @ngInject */
    function RoleManagementDeleteController($uibModalInstance, Role, entity) {
        var vm = this;

        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete() {
            Role.delete({id: entity.id},
                function () {
                    $uibModalInstance.close(true);
                },
                function () {
                    $uibModalInstance.close(false);
                });
        }
    }
})();
