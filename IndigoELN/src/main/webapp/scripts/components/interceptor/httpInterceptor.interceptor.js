angular.module('indigoeln')
    .factory('myHttpInterceptor', ['$q', '$rootScope', '$injector', function ($q, $rootScope, $injector) {
        return {
            'request': function (config) {
                return config;
            },
            'requestError': function (rejection) {
                return $q.reject(rejection);
            },
            'response': function (response) {
                return response;
            },

            'responseError': function (rejection) {
                if (rejection.status === 401 && rejection.config.url !== 'login') {
                    var $location = $injector.get('$location');
                    $location.path('/login');
                }
                return $q.reject(rejection);
            }
        };
    }]);