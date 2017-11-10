/* @ngInject */
function RoleManagementDeleteController($uibModalInstance, roleService, entity) {
    var vm = this;

    vm.clear = clear;
    vm.confirmDelete = confirmDelete;

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }

    function confirmDelete() {
        roleService.delete({
                id: entity.id
            },
            function() {
                $uibModalInstance.close(true);
            },
            function() {
                $uibModalInstance.close(false);
            });
    }
}

module.exports = RoleManagementDeleteController;
