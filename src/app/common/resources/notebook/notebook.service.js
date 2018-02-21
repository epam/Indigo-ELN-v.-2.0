/* @ngInject */
function notebookService($resource, permissionService, apiUrl, entityTreeService) {
    return $resource(apiUrl + 'projects/:projectId/notebooks/:notebookId', {}, {
        get: {
            method: 'GET',
            transformResponse: function(data) {
                var notebook = angular.fromJson(data);
                notebook.accessList = _.sortBy(notebook.accessList, function(value) {
                    return value.user.id;
                });

                return notebook;
            }
        },
        save: {
            method: 'POST',
            transformRequest: transformRequest,
            transformResponse: function(data) {
                var notebook = angular.fromJson(data);
                entityTreeService.addNotebookByEntity(notebook);

                return notebook;
            }
        },
        update: {
            method: 'PUT',
            url: apiUrl + 'projects/:projectId/notebooks',
            transformRequest: transformRequest,
            transformResponse: function(data) {
                var notebook = angular.fromJson(data);
                entityTreeService.updateNotebookByEntity(notebook);

                return notebook;
            }
        },
        delete: {
            method: 'DELETE'
        },
        print: {
            method: 'GET',
            url: apiUrl + 'print/project/:projectId/notebook/:notebookId'
        },
        isNew: {
            method: 'GET',
            url: apiUrl + 'notebooks/new'
        }
    });

    function transformRequest(data) {
        var newData = angular.copy(data);
        newData.accessList = permissionService.expandPermission(newData.accessList);

        return angular.toJson(newData);
    }
}

module.exports = notebookService;
