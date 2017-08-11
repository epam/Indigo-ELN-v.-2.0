angular.module('indigoeln')
    .config(function($stateProvider, PermissionManagementConfig, PermissionViewManagementConfig, userPermissions, printModal) {
        var permissions = [
            userPermissions.VIEWER,
            userPermissions.USER,
            userPermissions.OWNER
        ];

        var data = {
            authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
        };

        $stateProvider
            .state('notebook', {
                abstract: true,
                parent: 'entity'
            })
            .state('entities.notebook-new', {
                url: '/project/{parentId}/notebook/new',
                views: {
                    tabContent: {
                        templateUrl: 'scripts/app/entities/notebook/notebook-dialog.html',
                        controller: 'NotebookDialogController',
                        controllerAs: 'vm'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'New Notebook',
                        service: 'Notebook',
                        kind: 'notebook',
                        type: 'entity',
                        state: 'entities.notebook-new'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, Principal) {
                        var deferred = $q.defer();
                        $q.all([
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR')
                        ]).then(function(results) {
                            deferred.resolve({
                                notebook: {},
                                identity: results[0],
                                isContentEditor: results[1],
                                hasEditAuthority: results[2],
                                hasCreateChildAuthority: results[3],
                                experiments: {},
                                projectId: $stateParams.parentId
                            });
                        });

                        return deferred.promise;
                    }
                }
            })
            .state('entities.notebook-detail', {
                url: '/project/{projectId}/notebook/{notebookId}',
                views: {
                    tabContent: {
                        templateUrl: 'scripts/app/entities/notebook/notebook-dialog.html',
                        controller: 'NotebookDialogController',
                        controllerAs: 'vm'
                    }
                },
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Notebook',
                        service: 'Notebook',
                        kind: 'notebook',
                        type: 'entity',
                        state: 'entities.notebook-detail'
                    }
                },
                resolve: {
                    pageInfo: function($q, $stateParams, Principal, Notebook, EntitiesCache, NotebookSummaryExperiments,
                                       AutoSaveEntitiesEngine, EntitiesBrowser) {
                        if (!EntitiesCache.get($stateParams)) {
                            EntitiesCache.put($stateParams, AutoSaveEntitiesEngine.autoRecover(Notebook, $stateParams));
                        }
                        return $q
                            .all([
                                EntitiesCache.get($stateParams),
                                Principal.identity(),
                                Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                                Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR'),
                                Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR'),
                                EntitiesBrowser.getTabByParams($stateParams)
                            ])
                            .then(function(results) {
                                return {
                                    notebook: results[0],
                                    identity: results[1],
                                    isContentEditor: results[2],
                                    hasEditAuthority: results[3],
                                    hasCreateChildAuthority: results[4],
                                    dirty: results[5] ? results[5].dirty : false,
                                    projectId: $stateParams.projectId
                                };
                            });
                    }
                }
            })
            .state('entities.notebook-detail.print', _.extend({}, printModal, {
                parent: 'entities.notebook-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR']
                }
            }))
            .state('entities.notebook-new.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.notebook-new',
                data: data,
                permissions: permissions
            }))
            .state('entities.notebook-new.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.notebook-new',
                data: data,
                permissions: permissions
            }))
            .state('entities.notebook-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.notebook-detail',
                data: data,
                permissions: permissions
            }))
            .state('entities.notebook-detail.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.notebook-detail',
                data: data,
                permissions: permissions
            }));
    });
