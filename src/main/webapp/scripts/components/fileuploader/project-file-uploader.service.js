angular
    .module('indigoeln')
    .factory('ProjectFileUploaderService', projectFileUploaderService);

/* @ngInject */
function projectFileUploaderService($resource) {
    return $resource('api/project_files/:id', {}, {
        query: {
            method: 'GET', isArray: true
        },
        get: {
            method: 'GET',
            transformResponse: function(data) {
                data = angular.fromJson(data);

                return data;
            }
        },
        save: {
            method: 'POST'
        },
        update: {
            method: 'PUT'
        },
        delete: {
            method: 'DELETE'
        }
    });
}
