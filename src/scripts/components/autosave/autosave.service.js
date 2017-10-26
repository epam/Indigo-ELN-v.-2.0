angular
    .module('indigoeln')
    .factory('AutosaveService', function($resource, apiUrl) {
        return $resource(apiUrl + 'autosave/:id', {
            id: '@id'
        }, {
            save: {
                method: 'PUT'
            },
            get: {
                method: 'GET'
            },
            delete: {
                method: 'DELETE'
            }
        });
    });
