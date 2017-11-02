angular
    .module('indigoeln')
    .factory('UserRemovableFromProject', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    })
