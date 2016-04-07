'use strict';

angular.module('indigoeln')
    .factory('Config', function ($resource) {
        return $resource('api/client_configuration', {}, {
            'load': { method: 'GET'}
        });
    });
