angular
    .module('indigoeln')
    .factory('Notebook', function ($resource, PermissionManagement) {
        function transformRequest(data) {
            data.accessList = PermissionManagement.expandPermission(data.accessList);
        }

        return $resource('api/projects/:projectId/notebooks/:notebookId', {
            projectId: '@projectId'
        }, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    transformRequest(data);
                    return angular.toJson(data);
                }
            },
            'update': {
                method: 'PUT',
                url: 'api/projects/:projectId/notebooks',
                transformRequest: function (data) {
                    transformRequest(data);
                    return angular.toJson(data);
                }
            },
            'delete': {method: 'DELETE'}
        });
    });
