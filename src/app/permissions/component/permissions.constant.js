/* @ngInject */
var permissionsConfig = {
    url: '/permissions',
    onEnter: ['$rootScope', '$state', 'permissionModal', 'permissionService',
        function($rootScope, $state, permissionModal, permissionService) {
            var that = this;

            permissionModal.showEditPopup(that.permissions)
                .then(function(result) {
                    permissionService.setAccessList(result);
                    $rootScope.$broadcast('access-list-changed');
                })
                .finally(function() {
                    $state.go('^');
                });
        }],
    onExit: ['permissionModal',
        function(permissionModal) {
            permissionModal.close();
        }]
};

module.exports = permissionsConfig;
