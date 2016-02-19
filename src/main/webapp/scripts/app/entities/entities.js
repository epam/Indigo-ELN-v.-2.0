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
                    entities: function ($stateParams, EntitiesBrowser) {
                        return EntitiesBrowser.resolve($stateParams);
                    }
                }
            });
    });
