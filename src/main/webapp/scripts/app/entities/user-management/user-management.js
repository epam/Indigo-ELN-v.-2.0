angular.module('indigoeln')
    .config(function ($stateProvider) {
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
                        type:'entity'
                    }
                },
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/user-management/user-management.html',
                        controller: 'UserManagementController',
                        controllerAs: "vm"
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
            .state('entities.user-management.delete', {
                parent: 'entities.user-management',
                url: '/{login}/delete',
                data: {
                    authorities: ['USER_EDITOR'],
                    tab: {
                        type:''
                    }
                },
                onEnter: ['$stateParams', '$state', '$uibModal', function ($stateParams, $state, $uibModal) {
                    $uibModal.open({
                        templateUrl: 'scripts/app/entities/user-management/user-management-delete-dialog.html',
                        controller: 'UserManagementDeleteController',
                        controllerAs: "vm",
                        size: 'md',
                        resolve: {
                            entity: ['User', function (User) {
                                return User.get({login: $stateParams.login});
                            }]
                        }
                    }).result.then(function () {
                            $state.go('entities.user-management', null, {reload: true});
                        }, function () {
                            $state.go('^');
                        });
                }]
            });
    });
