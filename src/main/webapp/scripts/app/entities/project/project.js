'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('project', {
                parent: 'entity',
                url: '/project/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    project: function($stateParams, Project) {
                        return $stateParams.id ?  Project.get({id : $stateParams.id}).$promise : {};
                    },
                    identity: function (Principal) {
                        return Principal.identity()
                    },
                    editEnabled: function(PermissionManagement) {
                        return PermissionManagement.hasPermission('UPDATE_ENTITY');
                    }
                }
            })
            .state('project.permissions', {
                parent: 'project',
                url: '/permissions',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                onEnter: function($rootScope, $stateParams, $state, $uibModal, PermissionManagement) {
                    $uibModal.open({
                        templateUrl: 'scripts/components/permissions/permission-management.html',
                        controller: 'PermissionManagementController',
                        size: 'lg',
                        resolve: {
                            users: function(User) {
                                return User.query().$promise;
                            },
                            permissions: function() {
                                return PermissionManagement.getProjectPermissions();
                            }
                        }
                    }).result.then(function(result) {
                        PermissionManagement.setAccessList(result);
                        $rootScope.$broadcast('access-list-changed');
                        $state.go('project', {id: $stateParams.id});
                    }, function() {
                        $state.go('^');
                    })
                }
            });
    });