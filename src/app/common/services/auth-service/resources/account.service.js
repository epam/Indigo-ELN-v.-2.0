/* @ngInject */
function accountService($resource, apiUrl) {
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
}

module.exports = accountService;
