angular.module('indigoeln')
    .config(function ($stateProvider, PermissionManagementConfig, PermissionViewManagementConfig) {
        $stateProvider
            .state('project', {
                abstract: true,
                parent: 'entity'
            })
            .state('entities.project-new', {
                url: '/project/new',
                views: {
                    'tabContent': {
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
                    }
                },
                resolve: {
                    pageInfo: function ($q, Principal) {
                        var deferred = $q.defer();
                        $q.all([
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                        ]).then(function (results) {
                            deferred.resolve({
                                project: {},
                                identity: results[0],
                                isContentEditor: results[1],
                                hasEditAuthority: results[2],
                                hasCreateChildAuthority: results[3]
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('entities.project-detail', {
                url: '/project/{projectId}',
                views: {
                    'tabContent': {
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
                    pageInfo: function ($q, $stateParams, Principal, Project, EntitiesCache, AutoSaveEntitiesEngine, EntitiesBrowser) {

                        var deferred = $q.defer();
                        if (!EntitiesCache.get($stateParams)) {
                            EntitiesCache.put($stateParams, AutoSaveEntitiesEngine.autoRecover(Project, $stateParams));
                        }
                        $q.all([
                            EntitiesCache.get($stateParams),
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR'),
                            EntitiesBrowser.getTabByParams($stateParams)
                        ]).then(function (results) {
                            deferred.resolve({
                                project: results[0],
                                identity: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
                                hasCreateChildAuthority: results[4],
                                dirty: results[5] ? results[5].dirty : false
                            });
                        });
                        return deferred.promise;
                    }
                }
            })
            .state('entities.project-new.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.project-new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'USER', name: 'USER (read project, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, create notebooks)'}
                ]
            }))
            .state('entities.project-new.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.project-new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'USER', name: 'USER (read project, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, create notebooks)'}
                ]
            }))
            .state('entities.project-detail.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.project-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                'permissions': [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'USER', name: 'USER (read project, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, create notebooks)'}
                ]
            }))
            .state('entities.project-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.project-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                'permissions': [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'USER', name: 'USER (read project, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, create notebooks)'}
                ]
            }))
        ;
    });