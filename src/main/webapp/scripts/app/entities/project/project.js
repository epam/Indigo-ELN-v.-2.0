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
                    hasEditAuthority: function () {
                        return true;
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
                    hasEditAuthority: function (Principal) {
                        return Principal.hasAnyAuthority(['CONTENT_EDITOR', 'PROJECT_CREATOR']);
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
                    {id: 'USER', name: 'USER (read project and notebooks, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read and update project, read and create notebooks)'}
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
                    {id: 'USER', name: 'USER (read project and notebooks, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read and update project, read and create notebooks)'}
                ]
            }))
        ;
    });