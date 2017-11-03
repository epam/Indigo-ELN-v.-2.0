angular
    .module('indigoeln.permissionsModule')
    .factory('userRemovableFromProject', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    });
