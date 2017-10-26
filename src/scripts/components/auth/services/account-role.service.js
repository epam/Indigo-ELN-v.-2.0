angular
    .module('indigoeln')
    .factory('AccountRole', function($resource, apiUrl) {
        return $resource(apiUrl + 'accounts/account/roles', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
