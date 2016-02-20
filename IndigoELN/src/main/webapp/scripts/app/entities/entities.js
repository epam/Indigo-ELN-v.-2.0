'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('entities', {
                abstract: true,
                parent: 'entity',
                //url: '/project/{projectId}/notebook/{notebookId}/experiment/{experimentId}',
                params: {
                    projectId: null,
                    notebookId: null,
                    experimentId: null
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/entities.html',
                        controller: 'EntitiesController'
                    }
                },
                resolve: {
                    data: function ($stateParams, EntitiesBrowser, $q) {
                        var deferred = $q.defer();
                        EntitiesBrowser
                            .resolveTabs($stateParams)
                            .then(function (tabs) {
                                var kind = EntitiesBrowser.getKind($stateParams);
                                if (kind == 'experiment') {
                                    EntitiesBrowser.resolveFromCache({
                                        projectId: $stateParams.projectId,
                                        notebookId: $stateParams.notebookId
                                    }).then(function () {
                                        deferred.resolve({
                                            entities: tabs
                                        })
                                    });
                                } else {
                                    deferred.resolve({
                                        entities: tabs
                                    })
                                }
                            });
                        return deferred.promise;
                    }
                }
            });
    });
