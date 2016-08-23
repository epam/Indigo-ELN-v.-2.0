angular.module('indigoeln')
    .factory('AutosaveService', function ($resource) {
        return $resource('api/autosave/:id', {
            id: '@id'
        }, {
            'save': {method: 'PUT'},
            'get': {method: 'GET'},
            'delete': {method: 'DELETE'}
        });
    });
