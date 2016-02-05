'use strict';

angular.module('indigoeln')
    .factory('Experiment', function ($resource, DateUtils) {
        return $resource('api/experiments/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': {method: 'PUT'}
        });
    });
