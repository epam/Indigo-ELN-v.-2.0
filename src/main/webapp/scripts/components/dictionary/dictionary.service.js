angular.module('indigoeln')
    .factory('Dictionary', function ($resource) {
        return $resource('api/dictionaries/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {method: 'GET'},
            'all': {url: 'api/dictionaries/all', method: 'GET', isArray: true},
            'getByName': {url:'api/dictionaries/byName/:name', method: 'GET'},
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    });