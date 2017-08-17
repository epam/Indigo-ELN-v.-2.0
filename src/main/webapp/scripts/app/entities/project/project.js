angular
    .module('indigoeln')
    .config(function($stateProvider, PermissionManagementConfig, PermissionViewManagementConfig, userPermissions) {
        var permissions = [
            userPermissions.VIEWER,
            userPermissions.USER,
            userPermissions.OWNER
        ];

        $stateProvider
            .state('project', {
                abstract: true,
                parent: 'entity'
            })
            .state('entities.project-new', {
                url: '/project/new',
                views: {
                    tabContent: {
                        templateUrl: 'scripts/app/entities/project/project.html',
                        controller: 'ProjectController',
                        controllerAs: 'vm'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'New Project',
                        service: 'Project',
                        kind: 'project',
                        type: 'entity',
                        state: 'entities.project-new'
                    },
                    isNew: true
                },
                resolve: {
                    pageInfo: function($q, Principal) {
                        return $q.all([
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                        ]).then(function(results) {
                            return {
                                project: {},
                                identity: results[0],
                                isContentEditor: results[1],
                                hasEditAuthority: results[2],
                                hasCreateChildAuthority: results[3]
                            };
                        });
                    }
                }
            })
            .state('entities.project-detail', {
                url: '/project/{projectId}',
                views: {
                    tabContent: {
                        templateUrl: 'scripts/app/entities/project/project.html',
                        controller: 'ProjectController',
                        controllerAs: 'vm'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER', 'PROJECT_CREATOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Project',
                        service: 'Project',
                        kind: 'project',
                        type: 'entity',
                        state: 'entities.project-detail'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, Principal, Project) {
                        return $q.all([
                            Project.get($stateParams).$promise,
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                        ]).then(function(results) {
                            return {
                                project: results[0],
                                identity: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
                                hasCreateChildAuthority: results[4]
                            };
                        });
                    }
                }
            })
            .state('entities.project-detail.print', {
                parent: 'entities.project-detail',
                url: '/print',
                onEnter: function(printModal) {
                    printModal.showPopup();
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR']
                }
            })
            .state('entities.project-new.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.project-new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: permissions
            }))
            .state('entities.project-new.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.project-new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: permissions
            }))
            .state('entities.project-detail.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.project-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: permissions
            }))
            .state('entities.project-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.project-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: permissions
            }))
        ;
    });
