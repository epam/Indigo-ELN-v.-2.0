angular.module('indigoeln')
    .factory('Dashboard', function($resource, apiUrl) {
        return $resource(apiUrl + 'dashboard', {}, {
            get: {
                method: 'GET'
            }
        });
    });
