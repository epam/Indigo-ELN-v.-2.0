'use strict';

angular
    .module('indigoeln')
    .factory('Notebook', function ($resource) {
        return $resource('api/projects/:projectId/notebooks/:id', {
            projectId: '@projectId'
        }, {
            'query': {method: 'GET', isArray: true},
            'get': {method: 'GET'},
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    });
