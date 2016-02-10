'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('role-management', {
                parent: 'admin',
                url: '/role-management',
                data: {
                    authorities: ['ROLE_EDITOR'],
                    pageTitle: 'indigoeln'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/admin/role-management/role-management.html',
                        controller: 'RoleManagementController'
                    }
                },
                resolve: {
                    roles: ['Role', function (Role) {
                        return Role.query().$promise;
                    }],
                    accountRoles: ['AccountRole', function (AccountRole) {
                        return AccountRole.query().$promise;
                    }]
                }
            })
            .state('role-management.delete', {
                parent: 'role-management',
                url: '/role/{id}/delete',
                data: {
                    authorities: ['ROLE_EDITOR']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/admin/role-management/role-management-delete-dialog.html',
                        controller: 'role-managementDeleteController',
                        size: 'md',
                        resolve: {
                            entity: ['Role', function (Role) {
                                return Role.get({id: $stateParams.id}).$promise;
                            }]
                        }
                    }).result.then(function (result) {
                            $state.go('role-management', null, {reload: true});
                        }, function () {
                            $state.go('^');
                        });
                }]
            });
    });
