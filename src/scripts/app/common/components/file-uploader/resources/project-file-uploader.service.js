angular
    .module('indigoeln.fileUploader')
    .factory('projectFileUploaderService', projectFileUploaderService);

/* @ngInject */
function projectFileUploaderService($resource, apiUrl) {
    return $resource(apiUrl + 'project_files/:id', {}, {
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
