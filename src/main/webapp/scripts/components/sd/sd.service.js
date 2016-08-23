angular.module('indigoeln')
    .factory('SdService', function ($resource) {
        return $resource('api/sd', {}, {
            'export': {url: 'api/sd/export', method: 'POST'}
        });
    });
