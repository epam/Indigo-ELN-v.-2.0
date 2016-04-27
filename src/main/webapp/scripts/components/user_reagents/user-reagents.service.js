'use strict';

angular.module('indigoeln')
    .factory('UserReagents', function ($resource) {
        return $resource('api/user_reagents', {}, {
            'get': {
                method: 'GET', isArray: true
            },
            'save': {method: 'POST'}
        });
    });
