angular
    .module('indigoeln')
    .factory('UserRemovableFromNotebook', function($resource, apiUrl) {
        return $resource(apiUrl + 'notebooks/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    });
