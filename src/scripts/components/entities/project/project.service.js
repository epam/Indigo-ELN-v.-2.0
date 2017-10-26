angular
    .module('indigoeln')
    .factory('Project', project);

/* @ngInject */
function project($resource, FileUploaderCash, PermissionManagement, entityTreeService, apiUrl) {
    function transformRequest(data) {
        data = _.extend({}, data);
        data.tags = _.map(data.tags, 'text');
        data.fileIds = _.map(FileUploaderCash.getFiles(), 'id');
        data.accessList = PermissionManagement.expandPermission(data.accessList);

        return data;
    }

    function sortAccessList(accessList) {
        return _.sortBy(accessList, function(value) {
            return value.user.id;
        });
    }

    function transformResponse(data) {
        _.each(data.tags, function(tag, i) {
            data.tags[i] = {
                text: tag
            };
        });
        // assetsList is sorted by random on BE
        data.accessList = sortAccessList(data.accessList);
        _.forEach(data.notebooks, function(notebook) {
            notebook.accessList = sortAccessList(notebook.accessList);
        });
    }

    return $resource(apiUrl + 'projects/:projectId', {}, {
        query: {
            method: 'GET', isArray: true
        },
        get: {
            method: 'GET',
            transformResponse: function(data) {
                data = angular.fromJson(data);
                transformResponse(data);

                return data;
            }
        },
        save: {
            method: 'POST',
            transformRequest: function(data) {
                data = transformRequest(data);

                return angular.toJson(data);
            },
            interceptor: {
                response: function(response) {
                    entityTreeService.addNotebook(response.data);

                    return response.data;
                }
            }
        },
        update: {
            method: 'PUT',
            url: apiUrl + 'projects',
            transformRequest: function(data) {
                data = transformRequest(data);

                return angular.toJson(data);
            },
            transformResponse: function(data) {
                data = angular.fromJson(data);
                transformResponse(data);

                return data;
            },
            interceptor: {
                response: function(response) {
                    entityTreeService.updateNotebook(response.data);

                    return response.data;
                }
            }
        },
        delete: {
            method: 'DELETE'
        },
        print: {
            method: 'GET',
            url: apiUrl + 'print/project/:projectId'
        }
    });
}
