/* @ngInject */
function PermissionViewController($uibModalInstance, permissionService) {
    var vm = this;

    vm.accessList = permissionService.getAccessList();
    vm.entity = permissionService.getEntity();

    vm.close = close;

    function close() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = PermissionViewController;
