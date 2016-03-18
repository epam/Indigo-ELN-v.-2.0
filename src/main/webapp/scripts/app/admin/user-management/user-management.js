'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('user-management', {
                parent: 'tab',
                url: '/user-management',
                data: {
                    authorities: ['USER_EDITOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Users',
                        kind: 'management',
                        state: 'user-management'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/admin/user-management/user-management.html',
                        controller: 'UserManagementController'
                    }
                },
                resolve: {
                    pageInfo: function($q, Role) {
                        var deferred = $q.defer();
                        $q.all([
                            Role.query().$promise
                        ]).then(function(results){
                            deferred.resolve({
                                roles: results[0]
                            });
                        });
                        return deferred.promise;
                    }
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
