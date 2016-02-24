'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider, PermissionManagementConfig) {
        $stateProvider
            .state('notebook', {
                abstract: true,
                parent: 'entity'
            })
            .state('notebook.new', {
                parent: 'notebook',
                url: '/project/{projectId}/notebook/new',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/notebook/notebook-dialog.html',
                        controller: 'NotebookDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    notebook: function () {
                        return {};
                    },
                    identity: function (Principal) {
                        return Principal.identity();
                    },
                    isContentEditor: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR');
                    },
                    hasEditAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR');
                    },
                    hasCreateChildAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR');
                    }
                }
            }).state('entities.notebook-detail', {
                url: '/project/{projectId}/notebook/{notebookId}',
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/notebook/notebook-dialog.html',
                        controller: 'NotebookDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    notebook: function ($stateParams, EntitiesBrowser) {
                        return EntitiesBrowser.getCurrentEntity($stateParams);
                    },
                    identity: function (Principal) {
                        return Principal.identity();
                    },
                    isContentEditor: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR');
                    },
                    hasEditAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR');
                    },
                    hasCreateChildAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR');
                    }
                }
            })
            .state('notebook.new.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'notebook.new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read notebook)'},
                    {id: 'CHILD_VIEWER', name: 'CHILD_VIEWER (read notebook and experiments)'},
                    {id: 'USER', name: 'USER (read notebook and experiments, create experiments)'},
                    {id: 'OWNER', name: 'OWNER (read and update notebook, read and create experiments)'}
                ]
            }))
            .state('entities.notebook-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.notebook-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read notebook)'},
                    {id: 'CHILD_VIEWER', name: 'CHILD_VIEWER (read notebook and experiments)'},
                    {id: 'USER', name: 'USER (read notebook and experiments, create experiments)'},
                    {id: 'OWNER', name: 'OWNER (read and update notebook, read and create experiments)'}
                ]
            }))
            .state('notebook.select-project', {
                parent: 'notebook',
                url: 'notebook/select-project',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                onEnter: function($state, $uibModal, $window) {
                    $uibModal.open({
                        animation: true,
                        templateUrl: 'scripts/app/entities/notebook/notebook-select-parent.html',
                        controller: 'NotebookSelectParentController',
                        size: 'lg',
                        resolve: {
                            parents: function (ProjectsForSubCreation) {
                                return ProjectsForSubCreation.query().$promise;
                            }
                        }
                    }).result.then(function (projectId) {
                        $state.go('notebook.new', {projectId: projectId});
                    }, function() {
                        $window.history.back();
                    });
                }
            });
    });
