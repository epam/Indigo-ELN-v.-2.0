var template = require('./permissions.html');

/* @ngInject */
var permissionsConfig = {
    url: '/permissions',
    onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'permissionService',
        function($rootScope, $stateParams, $state, $uibModal, permissionService) {
            var that = this;
            $uibModal.open({
                template: template,
                controller: 'PermissionsController',
                controllerAs: 'vm',
                size: 'lg',
                resolve: {
                    users: function(userWithAuthorityService) {
                        return userWithAuthorityService.query().$promise;
                    },
                    permissions: function() {
                        return that.permissions;
                    }
                }
            })
                .result
                .then(function(result) {
                    permissionService.setAccessList(result);
                    $rootScope.$broadcast('access-list-changed');
                })
                .finally(function() {
                    $state.go('^');
                });
        }]
};

module.exports = permissionsConfig;
