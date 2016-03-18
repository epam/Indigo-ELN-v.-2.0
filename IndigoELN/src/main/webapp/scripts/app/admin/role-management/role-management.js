'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('role-management', {
                parent: 'tab',
                url: '/role-management',
                data: {
                    authorities: ['ROLE_EDITOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Roles',
                        kind: 'management',
                        state: 'role-management'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/admin/role-management/role-management.html',
                        controller: 'RoleManagementController'
                    }
                },
                resolve: {
                    pageInfo: function($q, Role, AccountRole) {
                        var deferred = $q.defer();
                        $q.all([
                            Role.query().$promise,
                            AccountRole.query().$promise
                        ]).then(function(results){
                            deferred.resolve({
                                roles: results[0],
                                accountRoles: results[1]
                            });
                        });
                        return deferred.promise;
                    }
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
