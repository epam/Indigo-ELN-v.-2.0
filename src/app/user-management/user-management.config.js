var userManagementTemplate = require('./component/user-management.html');
var userManagementDeleteDialogTemplate = require('./delete-dialog/user-management-delete-dialog.html');

userManagementConfig.$inject = ['$stateProvider'];

function userManagementConfig($stateProvider) {
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
                    template: userManagementTemplate,
                    controller: 'UserManagementController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                passwordRegex: function(userPasswordValidationService) {
                    return userPasswordValidationService.get().$promise
                        .then(function(response) {
                            return response.data;
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
                    template: userManagementDeleteDialogTemplate,
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
}

module.exports = userManagementConfig;
