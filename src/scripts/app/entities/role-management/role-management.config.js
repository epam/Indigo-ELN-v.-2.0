var roleManagementTemplate = require('./role-management.html');
var roleManagementDeleteDialog = require('./role-management-delete-dialog.html');

roleManagementConfig.$inject = ['$stateProvider'];

function roleManagementConfig($stateProvider) {
    $stateProvider
        .state('entities.role-management', {
            url: '/role-management',
            data: {
                authorities: ['ROLE_EDITOR'],
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Roles',
                    kind: 'management',
                    state: 'entities.role-management',
                    type: 'entity'
                }
            },
            views: {
                tabContent: {
                    template: roleManagementTemplate,
                    controller: 'RoleManagementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                pageInfo: function($q, roleService, accountRole, authService) {
                    return $q.all([
                        roleService.query().$promise,
                        accountRole.query().$promise,
                        authService.getAuthorities()
                    ]).then(function(results) {
                        return {
                            roles: results[0],
                            accountRoles: results[1],
                            authorities: results[2]
                        };
                    });
                }
            }
        })
        .state('entities.role-management.delete', {
            parent: 'entities.role-management',
            url: '/role/{id}/delete',
            data: {
                authorities: ['ROLE_EDITOR'],
                tab: {
                    type: ''
                }
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    template: roleManagementDeleteDialog,
                    controller: 'RoleManagementDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['roleService', function(roleService) {
                            return roleService.get({
                                id: $stateParams.id
                            }).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('entities.role-management', null, {
                        reload: true
                    });
                }, function() {
                    $state.go('entities.role-management');
                });
            }]
        });
}

module.exports = roleManagementConfig;
