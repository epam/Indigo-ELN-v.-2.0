angular
    .module('indigoeln')
    .factory('userRemovableFromProject', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    });
