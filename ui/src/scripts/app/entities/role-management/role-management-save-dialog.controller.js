(function() {
    angular
        .module('indigoeln')
        .controller('RoleManagementSaveController', RoleManagementSaveController);

    /* @ngInject */
    function RoleManagementSaveController($uibModalInstance) {
        var vm = this;

        vm.clear = clear;
        vm.confirmSave = confirmSave;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmSave() {
            $uibModalInstance.close(true);
        }
    }
})();
