angular
    .module('indigoeln.permissionsModule')
    .constant('permissionManagementConfig', {
        url: '/permissions',
        onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'permissionManagementService',
            function($rootScope, $stateParams, $state, $uibModal, permissionManagementService) {
                var that = this;
                $uibModal.open({
                    templateUrl: 'scripts/app/permissions/component/permission-management.html',
                    controller: 'PermissionManagementController',
                    controllerAs: 'vm',
                    size: 'lg',
                    resolve: {
                        users: function(userWithAuthority) {
                            return userWithAuthority.query().$promise;
                        },
                        permissions: function() {
                            return that.permissions;
                        }
                    }
                }).result.then(function(result) {
                    permissionManagementService.setAccessList(result);
                    $rootScope.$broadcast('access-list-changed');
                    $state.go('^');
                }, function() {
                    $state.go('^');
                });
            }]
    });
