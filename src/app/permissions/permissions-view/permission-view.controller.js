/* @ngInject */
function PermissionViewController($uibModalInstance, permissionService) {
    var vm = this;

    vm.accessList = _.sortBy(permissionService.getAccessList(), function(p) {
        return p.user.lastName;
    });
    vm.entity = permissionService.getEntity();

    vm.close = close;

    function close() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = PermissionViewController;
