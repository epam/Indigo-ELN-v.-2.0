angular.module('indigoeln')
    .config(function($stateProvider, PermissionManagementConfig, PermissionViewManagementConfig) {
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
            }).state('entities.notebook-detail', {
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
                        var deferred = $q.defer();
                        if (!EntitiesCache.get($stateParams)) {
                            EntitiesCache.put($stateParams, AutoSaveEntitiesEngine.autoRecover(Notebook, $stateParams));
                        }
                        $q.all([
                            EntitiesCache.get($stateParams),
                            Principal.identity(),
                            Principal.hasAuthorityIdentitySafe('CONTENT_EDITOR'),
                            Principal.hasAuthorityIdentitySafe('NOTEBOOK_CREATOR'),
                            Principal.hasAuthorityIdentitySafe('EXPERIMENT_CREATOR'),
                            EntitiesBrowser.getTabByParams($stateParams)
                        ]).then(function(results) {
                            deferred.resolve({
                                notebook: results[0],
                                identity: results[1],
                                isContentEditor: results[2],
                                hasEditAuthority: results[3],
                                hasCreateChildAuthority: results[4],
                                dirty: results[5] ? results[5].dirty : false,
                                projectId: $stateParams.projectId
                            });
                        });

                        return deferred.promise;
                    }
                }
            })
            .state('entities.notebook-new.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.notebook-new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                permissions: [
                    {
                        id: 'VIEWER', name: 'VIEWER (read notebook)'
                    },
                    {
                        id: 'USER', name: 'USER (read notebook, create experiments)'
                    },
                    {
                        id: 'OWNER', name: 'OWNER (read/update notebook, create experiments)'
                    }
                ]
            }))
            .state('entities.notebook-new.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.notebook-new',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                permissions: [
                    {
                        id: 'VIEWER', name: 'VIEWER (read notebook)'
                    },
                    {
                        id: 'USER', name: 'USER (read notebook, create experiments)'
                    },
                    {
                        id: 'OWNER', name: 'OWNER (read/update notebook, create experiments)'
                    }
                ]
            }))
            .state('entities.notebook-detail.permissions', _.extend({}, PermissionManagementConfig, {
                parent: 'entities.notebook-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                permissions: [
                    {
                        id: 'VIEWER', name: 'VIEWER (read notebook)'
                    },
                    {
                        id: 'USER', name: 'USER (read notebook, create experiments)'
                    },
                    {
                        id: 'OWNER', name: 'OWNER (read/update notebook, create experiments)'
                    }
                ]
            }))
            .state('entities.notebook-detail.permissions-view', _.extend({}, PermissionViewManagementConfig, {
                parent: 'entities.notebook-detail',
                data: {
                    authorities: ['CONTENT_EDITOR', 'NOTEBOOK_CREATOR']
                },
                permissions: [
                    {
                        id: 'VIEWER', name: 'VIEWER (read notebook)'
                    },
                    {
                        id: 'USER', name: 'USER (read notebook, create experiments)'
                    },
                    {
                        id: 'OWNER', name: 'OWNER (read/update notebook, create experiments)'
                    }
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
                        controllerAs: 'vm',
                        size: 'lg',
                        resolve: {
                            parents: function(ProjectsForSubCreation) {
                                return ProjectsForSubCreation.query().$promise;
                            }
                        }
                    }).result.then(function(projectId) {
                        $state.go('entities.notebook-new', {
                            parentId: projectId
                        });
                    }, function() {
                        $window.history.back();
                    });
                }
            });
    });
