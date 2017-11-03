angular
    .module('indigoeln.permissionsModule')
    .factory('userRemovableFromNotebook', function($resource, apiUrl) {
        return $resource(apiUrl + 'notebooks/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    });
