'use strict';

angular.module('indigoeln')
    .factory('Experiment', function ($resource, DateUtils) {
        return $resource('api/projects/:projectId/notebooks/:notebookId/experiments/:experimentId',
            {
                projectId: '@projectId',
                notebookId: '@notebookId',
                experimentId: '@experimentId'
            }, {
                'query': {method: 'GET', isArray: true},
                'get': {
                    method: 'GET',
                    transformResponse: function (data) {
                        data = angular.fromJson(data);
                        return data;
                    }
                },
                'update': {method: 'PUT'}
            });
    });
