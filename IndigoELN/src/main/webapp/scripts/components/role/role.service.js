angular.module('indigoeln')
    .factory('Role', function ($resource) {
        return $resource('api/roles/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {method: 'GET'},
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    });