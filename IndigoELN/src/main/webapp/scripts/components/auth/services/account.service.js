angular.module('indigoeln')
    .factory('Account', function ($resource) {
        return $resource('api/accounts/account', {}, {
            'get': {
                method: 'GET', params: {}, isArray: false,
                interceptor: {
                    response: function (response) {
                        // expose response
                        return response;
                    }
                }
            }
        });
    });
