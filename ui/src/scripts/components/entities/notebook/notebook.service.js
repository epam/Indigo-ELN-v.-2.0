angular
    .module('indigoeln')
    .factory('Notebook', notebook);

/* @ngInject */
function notebook($resource, PermissionManagement) {
    return $resource('api/projects/:projectId/notebooks/:notebookId', {
        projectId: '@projectId'
    }, {
        query: {
            method: 'GET', isArray: true
        },
        get: {
            method: 'GET',
            transformResponse: function(data) {
                data = angular.fromJson(data);
                data.accessList = _.sortBy(data.accessList, function(value) {
                    return value.user.id;
                });

                return data;
            }
        },
        save: {
            method: 'POST',
            transformRequest: function(data) {
                data = transformRequest(data);

                return angular.toJson(data);
            }
        },
        update: {
            method: 'PUT',
            url: 'api/projects/:projectId/notebooks',
            transformRequest: function(data) {
                data = transformRequest(data);

                return angular.toJson(data);
            }
        },
        delete: {
            method: 'DELETE'
        },
        print: {
            method: 'GET',
            url: 'api/print/project/:projectId/notebook/:notebookId'
        }
    });


    function transformRequest(data) {
        data = _.extend({}, data);
        data.accessList = PermissionManagement.expandPermission(data.accessList);

        return data;
    }
}
