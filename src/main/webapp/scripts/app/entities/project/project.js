'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('project', {
                abstract: true,
                parent: 'entity'
            })
            .state('project.new', {
                parent: 'project',
                url: '/project/new',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER', 'PROJECT_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    project: function () {
                        return {};
                    },
                    identity: function (Principal) {
                        return Principal.identity()
                    },
                    editEnabled: function (PermissionManagement) {
                        return PermissionManagement.hasPermission('UPDATE_ENTITY');
                    }
                }
            })
            .state('entities.project-detail', {
                url: '/project/{projectId}',
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER', 'PROJECT_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    project: function ($stateParams, EntitiesBrowser) {
                        return EntitiesBrowser.getCurrentEntity($stateParams);
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
                url: '/project/permissions',
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
                        $state.go('project.new', {id: $stateParams.id});
                    }, function() {
                        $state.go('^');
                    })
                }
            });
    });