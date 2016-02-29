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
            },
            addFile: function(file) {
                if (_files) {
                    _files.push(file);
                }
            },
            removeFile: function(file) {
                if (_files) {
                    _files = _.without(_files, file);
                }
            },
            addFiles: function(files) {
                if (_files) {
                    _files = _.union(_files, files);
                }
            }
        };

    });
