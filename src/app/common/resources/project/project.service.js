/* @ngInject */
function projectService($resource, fileUploaderCash, permissionManagementService, entityTreeService, apiUrl) {
    function transformRequest(data) {
        var newData = angular.copy(data);
        newData.tags = _.map(newData.tags, 'text');
        newData.fileIds = _.map(fileUploaderCash.getFiles(), 'id');
        newData.accessList = permissionManagementService.expandPermission(newData.accessList);

        return angular.toJson(newData);
    }

    function sortAccessList(accessList) {
        return _.sortBy(accessList, function(value) {
            return value.user.id;
        });
    }

    function transformResponse(data) {
        _.each(data.tags, function(tag, i) {
            data.tags[i] = {text: tag};
        });
        // assetsList is sorted by random on BE
        data.accessList = sortAccessList(data.accessList);
        _.forEach(data.notebooks, function(notebook) {
            notebook.accessList = sortAccessList(notebook.accessList);
        });

        return angular.fromJson(data);
    }

    return $resource(apiUrl + 'projects/:projectId', {}, {
        query: {
            method: 'GET', isArray: true
        },
        get: {
            method: 'GET',
            transformResponse: transformResponse
        },
        save: {
            method: 'POST',
            transformRequest: transformRequest,
            interceptor: {response: function(response) {
                entityTreeService.addNotebook(response.data);

                return response.data;
            }}
        },
        update: {
            method: 'PUT',
            url: apiUrl + 'projects',
            transformRequest: transformRequest,
            transformResponse: transformResponse,
            interceptor: {response: function(response) {
                entityTreeService.updateNotebook(response.data);

                return response.data;
            }}
        },
        delete: {method: 'DELETE'},
        print: {
            method: 'GET',
            url: apiUrl + 'print/project/:projectId'
        }
    });
}

module.exports = projectService;
