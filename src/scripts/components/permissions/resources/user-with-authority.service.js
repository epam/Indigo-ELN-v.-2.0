angular
    .module('indigoeln')
    .factory('UserWithAuthority', function($resource, apiUrl) {
        return $resource(apiUrl + 'users/permission-management', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
