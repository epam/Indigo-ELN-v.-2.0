'use strict';

angular.module('indigoeln')
    .factory('FileUploaderService', function ($resource) {
        return $resource('api/project_files/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    })
    .factory('FileUploaderCash', function () {
        var _files;

        return {
            getFiles: function() {
                return _files;
            },
            setFiles: function(files) {
                _files = files;
            }
        };

    });
