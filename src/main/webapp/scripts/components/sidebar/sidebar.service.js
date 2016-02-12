'use strict';

angular.module('indigoeln')
    .factory('AllProjects', function($resource) {
        return $resource('api/projects/all', {}, {
            'query': { method: 'GET', isArray: true}
        });
    })
    .factory('AllNotebooks', function ($resource) {
        return $resource('api/notebooks/all:id', {}, {
            'query': {method: 'GET', isArray: true}
        });
    })
    .factory('AllExperiments', function ($resource) {
        return $resource('api/notebooks/:notebookId/experiments/all',
            {
                notebookId: '@notebookId',
                experimentId: '@experimentId'
            }, {
                'query': {method: 'GET', isArray: true}
            });
    });
