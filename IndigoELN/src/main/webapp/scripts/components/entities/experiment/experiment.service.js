'use strict';

angular.module('indigoeln')
    .factory('Experiment', function($resource, DateUtils) {
        return $resource('api/experiments/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    data.creationDate = DateUtils.convertLocaleDateFromServer(data.creationDate);
                    data.lastEditDate = DateUtils.convertLocaleDateFromServer(data.lastEditDate);
                    return data;
                }
            },
            'post': {
                method: 'POST',
                transformRequest: function (data) {
                    data.creationDate = DateUtils.convertLocaleDateToServer(data.creationDate);
                    data.lastEditDate = DateUtils.convertLocaleDateToServer(data.lastEditDate);
                    return angular.toJson(data);
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    data.creationDate = DateUtils.convertLocaleDateToServer(data.creationDate);
                    data.lastEditDate = DateUtils.convertLocaleDateToServer(data.lastEditDate);
                    return angular.toJson(data);
                }
            }
        });
    });
