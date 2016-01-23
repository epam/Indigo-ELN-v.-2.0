'use strict';

angular.module('indigoeln')
    .factory('projectService', function ($resource) {
        return $resource('api/projects/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {method: 'GET'},
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    });