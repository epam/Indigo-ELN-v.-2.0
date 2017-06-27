(function() {
    angular
        .module('indigoeln')
        .controller('PermissionViewManagementController', PermissionViewManagementController);

    /* @ngInject */
    function PermissionViewManagementController($uibModalInstance, PermissionManagement) {
        var vm = this;

        vm.accessList = PermissionManagement.getAccessList();
        vm.entity = PermissionManagement.getEntity();

        vm.close = close;

        function close() {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
