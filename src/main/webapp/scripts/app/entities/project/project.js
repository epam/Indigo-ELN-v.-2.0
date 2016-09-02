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
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Project',
                        service:'Project',
                        kind: 'project'
                    }
                },
                resolve: {
                    pageInfo: function($q,Principal) {
                        var deferred = $q.defer();
                        $q.all([
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                        ]).then(function(results){
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
                        templateUrl: 'scripts/app/entities/project/project-dialog.html',
                        controller: 'ProjectDialogController'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER', 'PROJECT_CREATOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Project',
                        service:'Project',
                        kind: 'project',
                        type:'entity',
                        state: 'entities.project-detail'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, Principal, Project, EntitiesCache, AutoSaveEntitiesEngine) {

                        var deferred = $q.defer();
                        if(!EntitiesCache.get($stateParams)){
                            EntitiesCache.put($stateParams, AutoSaveEntitiesEngine.autoRecover(Project, $stateParams));
                        }
                        $q.all([
                            EntitiesCache.get($stateParams),
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('PROJECT_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR')
                        ]).then(function(results){
                            deferred.resolve({
                                project: results[0],
                                identity: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
                                hasCreateChildAuthority: results[4]
                            });
                        });
                        return deferred.promise;
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
                    {id: 'USER', name: 'USER (read project, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, create notebooks)'}
                ]
            }))
            .state('entities.project-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.project-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'PROJECT_CREATOR']
                },
                permissions: [
                    {id: 'VIEWER', name: 'VIEWER (read project)'},
                    {id: 'USER', name: 'USER (read project, create notebooks)'},
                    {id: 'OWNER', name: 'OWNER (read/update project, create notebooks)'}
                ]
            }))
        ;
    });