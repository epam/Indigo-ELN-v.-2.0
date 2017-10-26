angular.module('indigoeln')
    .factory('sdService', function($resource) {
        return $resource('api/sd', {}, {
            export: {
                url: 'api/sd/export', method: 'POST'
            }
        });
    });
