angular
    .module('indigoeln.userManagementModule', [])
    .config(function($stateProvider) {
        $stateProvider
            .state('entities.user-management', {
                url: '/user-management',
                data: {
                    authorities: ['USER_EDITOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Users',
                        kind: 'management',
                        state: 'entities.user-management',
                        type: 'entity'
                    }
                },
                views: {
                    tabContent: {
                        templateUrl: 'scripts/app/user-management/user-management.html',
                        controller: 'UserManagementController',
                        controllerAs: 'vm'
                    }
                },
                resolve: {
                    pageInfo: function($q, roleService) {
                        return $q.all([
                            roleService.query().$promise
                        ]).then(function(results) {
                            return {
                                roles: results[0]
                            };
                        });
                    }
                }
            })
            .state('entities.user-management.delete', {
                parent: 'entities.user-management',
                url: '/{login}/delete',
                data: {
                    authorities: ['USER_EDITOR'],
                    tab: {
                        type: ''
                    }
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/user-management/user-management-delete-dialog.html',
                        controller: 'UserManagementDeleteController',
                        controllerAs: 'vm',
                        size: 'md',
                        resolve: {
                            entity: ['userService', function(userService) {
                                return userService.get({
                                    login: $stateParams.login
                                });
                            }]
                        }
                    }).result.then(function() {
                        $state.go('entities.user-management', null, {
                            reload: true
                        });
                    }, function() {
                        $state.go('^');
                    });
                }]
            });
    });
