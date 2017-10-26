angular.module('indigoeln')
    .factory('Template', function($resource, apiUrl) {
        return $resource(apiUrl + 'templates/:id', {}, {
            query: {
                method: 'GET', isArray: true
            },
            get: {
                method: 'GET',
                transformResponse: function(data) {
                    data = angular.fromJson(data);

                    return data;
                }
            },
            update: {
                method: 'PUT'
            }
        });
    });
