angular.module('indigoeln')
    .factory('Template', function($resource) {
        return $resource('api/templates/:id', {}, {
            query: {
                method: 'GET', isArray: true
            },
            get: {
                method: 'GET',
                transformResponse: function(data) {
                    data = JSON.parse(data);

                    return data;
                }
            },
            update: {
                method: 'PUT'
            }
        });
    });
