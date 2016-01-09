'use strict';

angular
    .module('indigoeln')
    .factory('experimentService', experimentService);

experimentService.$inject = ['$resource'];

function experimentService($resource) {
    return $resource('service/experiments/:id', {}, {
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
}