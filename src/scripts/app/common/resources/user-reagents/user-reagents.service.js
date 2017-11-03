angular.module('indigoeln')
    .factory('userReagents', function($resource, apiUrl) {
        return $resource(apiUrl + 'user_reagents', {}, {
            get: {
                method: 'GET', isArray: true
            },
            save: {
                method: 'POST'
            }
        });
    });
