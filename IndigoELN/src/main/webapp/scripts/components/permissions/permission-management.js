angular.module('indigoeln')
    .constant('PermissionManagementConfig', {
        url: '/permissions',
        onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'PermissionManagement',
            function($rootScope, $stateParams, $state, $uibModal, PermissionManagement) {
                var that = this;
                $uibModal.open({
                    templateUrl: 'scripts/components/permissions/permission-management.html',
                    controller: 'PermissionManagementController',
                    controllerAs: 'vm',
                    size: 'lg',
                    resolve: {
                        users: function(UserWithAuthority) {
                            return UserWithAuthority.query().$promise;
                        },
                        permissions: function() {
                            return that.permissions;
                        }
                    }
                }).result.then(function(result) {
                    PermissionManagement.setAccessList(result);
                    $rootScope.$broadcast('access-list-changed');
                    $state.go('^');
                }, function() {
                    $state.go('^');
                });
            }]
    });
