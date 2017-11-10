var template = require('./permission-view.html');

var permissionViewManagementConfig = {
    url: '/permissions-view',
    onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'permissionManagementService',
        function($rootScope, $stateParams, $state, $uibModal) {
            $uibModal.open({
                template: template,
                controller: 'PermissionViewManagementController',
                controllerAs: 'vm',
                size: 'lg'
            }).result.then(function() {
                $state.go('^');
            }, function() {
                $state.go('^');
            });
        }]
};

module.exports = permissionViewManagementConfig;
