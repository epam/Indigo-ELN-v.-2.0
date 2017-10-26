angular.module('indigoeln')
    .factory('sdService', function($resource, apiUrl) {
        return $resource(apiUrl + 'sd', {}, {
            export: {
                url: apiUrl + 'sd/export', method: 'POST'
            }
        });
    });
