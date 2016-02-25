'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider, PermissionManagementConfig) {
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
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    project: function () {
                        return {};
                    },
                    identity: function (Principal) {
                        return Principal.identity();
                    },
                    isContentEditor: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR');
                    },
                    hasEditAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR');
                    },
                    hasCreateChildAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR');
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
                        return Principal.identity();
                    },
                    isContentEditor: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR');
                    },
                    hasEditAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR');
                    },
                    hasCreateChildAuthority: function (Principal) {
                        return Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR');
                    }
                }
            })
            .state('project.new.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'project.new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'CHILD_VIEWER', name: 'CHILD_VIEWER (read project and notebooks)'},
                    {id: 'USER', name: 'USER (read project/notebooks, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, read/create notebooks)'}
                ]
            }))
            .state('entities.project-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.project-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'CHILD_VIEWER', name: 'CHILD_VIEWER (read project and notebooks)'},
                    {id: 'USER', name: 'USER (read project/notebooks, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, read/create notebooks)'}
                ]
            }))
        ;
    });