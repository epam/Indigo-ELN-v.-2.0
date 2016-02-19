'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('notebook', {
                parent: 'entity',
                url: '/project/{projectId}/notebook/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/notebook/notebook-dialog.html',
                        controller: 'NotebookDialogController'
                    }
                },
                data: {
                    authorities: ['NOTEBOOK_READER', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    notebook: function($stateParams, Notebook) {
                        if ($stateParams.id) {
                            return Notebook.get({projectId: $stateParams.projectId, id: $stateParams.id});
                        } else {
                            return {
                                id: null,
                                name: null
                            }
                        }
                    },
                    identity: function (Principal) {
                        return Principal.identity()
                    }
                }
            })
            .state('notebook.permissions', {
                parent: 'notebook',
                url: '/permissions',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
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
                                return PermissionManagement.getNotebookPermissions();
                            }
                        }
                    }).result.then(function(result) {
                        PermissionManagement.setAccessList(result);
                        $rootScope.$broadcast('access-list-changed');
                        $state.go('notebook', {projectId: $stateParams.projectId, id: $stateParams.id});
                    }, function() {
                        $state.go('^');
                    })
                }
            })
            .state('select-project', {
                parent: 'entity',
                url: '/select-project',
                data: {
                    authorities: ['NOTEBOOK_READER', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                onEnter: function($state, $uibModal, $window) {
                    $uibModal.open({
                        animation: true,
                        templateUrl: 'scripts/app/entities/notebook/notebook-select-project.html',
                        controller: 'NotebookSelectProjectController',
                        size: 'lg',
                        resolve: {
                            projects: function (Project) {
                                return Project.query().$promise;
                            }
                        }
                    }).result.then(function (projectId) {
                        $state.go('notebook', {projectId: projectId});
                    }, function() {
                        $window.history.back();
                    })
                }
            })
    });
