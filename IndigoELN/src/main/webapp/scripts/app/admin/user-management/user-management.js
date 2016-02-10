'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('user-management', {
                parent: 'admin',
                url: '/user-management',
                data: {
                    authorities: ['USER_EDITOR'],
                    pageTitle: 'indigoeln'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/admin/user-management/user-management.html',
                        controller: 'UserManagementController'
                    }
                },
                resolve: {
                    roles: ['Role', function(Role) {
                        return Role.query().$promise;
                    }]
                }
            })
            .state('user-management.delete', {
                parent: 'user-management',
                url: '/{login}/delete',
                data: {
                    authorities: ['USER_EDITOR']
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/admin/user-management/user-management-delete-dialog.html',
                        controller: 'user-managementDeleteController',
                        size: 'md',
                        resolve: {
                            entity: ['User', function (User) {
                                return User.get({login: $stateParams.login});
                            }]
                        }
                    }).result.then(function (result) {
                            $state.go('user-management', null, {reload: true});
                        }, function () {
                            $state.go('^');
                        });
                }]
            });
    });
