angular
    .module('indigoeln')
    .factory('userWithAuthority', function($resource, apiUrl) {
        return $resource(apiUrl + 'users/permission-management', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
