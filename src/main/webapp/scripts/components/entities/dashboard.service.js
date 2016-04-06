'use strict';

angular.module('indigoeln')
    .factory('Dashboard', function ($resource) {
        return $resource('api/dashboard', {}, {
                'get': {
                    method: 'GET'
                }
            });
    });
