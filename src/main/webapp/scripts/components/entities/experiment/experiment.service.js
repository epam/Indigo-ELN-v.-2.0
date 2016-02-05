'use strict';

angular.module('indigoeln')
    .factory('Experiment', function ($resource, DateUtils) {
        return $resource('api/notebooks/:notebookId/experiments/:experimentId',
            {
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
