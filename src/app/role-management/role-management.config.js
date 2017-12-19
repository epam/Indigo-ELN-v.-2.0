var roleManagementTemplate = require('./component/role-management.html');

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
                pageInfo: function($q, accountRoleService, authService) {
                    return $q.all([
                        accountRoleService.query().$promise,
                        authService.getAuthorities()
                    ]).then(function(results) {
                        return {
                            accountRoles: results[0],
                            authorities: results[1]
                        };
                    });
                }
            }
        });
}

module.exports = roleManagementConfig;
