/* @ngInject */
function authExpiredInterceptor($rootScope, $q, $cookies, $injector) {
    return {
        responseError: responseError
    };

    function responseError(response) {
        // If we have an unauthorized request we redirect to the login page
        // Don't do this check on the account API to avoid infinite loop
        if (response.status === 401
            && !_.isUndefined(response.data.path)
            && response.data.path.indexOf('/api/account') === -1
        ) {
            var authService = $injector.get('authService');
            var $state = $injector.get('$state');
            var to = $rootScope.toState;
            var params = $rootScope.toStateParams;
            authService.logout();
            $rootScope.previousStateName = to;
            $rootScope.previousStateNameParams = params;
            $state.go('login');
        } else if (response.status === 403 && response.config.method !== 'GET' && getCSRF() === '') {
            // If the CSRF token expired, then try to get a new CSRF token and retry the old request
            var $http = $injector.get('$http');

            return $http.get('/').finally(function() {
                return afterCSRFRenewed(response);
            });
        }

        return $q.reject(response);
    }

    function afterCSRFRenewed(oldResponse) {
        if (getCSRF() !== '') {
            // retry the old request after the new CSRF-TOKEN is obtained
            var $http = $injector.get('$http');

            return $http(oldResponse.config);
        }
        // unlikely get here but reject with the old response any way and avoid infinite loop

        return $q.reject(oldResponse);
    }

    function getCSRF() {
        return $cookies.get('CSRF-TOKEN') || '';
    }
}

module.exports = authExpiredInterceptor;