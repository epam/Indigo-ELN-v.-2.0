angular.module('indigoeln')
    .factory('Dictionary', function ($resource) {
        return $resource('api/dictionaries/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {method: 'GET'},
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    });