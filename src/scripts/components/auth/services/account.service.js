angular
    .module('indigoeln')
    .factory('Account', function($resource, apiUrl) {
        return $resource(apiUrl + 'accounts/account', {}, {
            get: {
                method: 'GET',
                params: {},
                isArray: false,
                interceptor: {
                    response: function(response) {
                        // expose response
                        return response;
                    }
                }
            }
        });
    });
