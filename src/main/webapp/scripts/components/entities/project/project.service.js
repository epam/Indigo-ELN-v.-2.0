angular
    .module('indigoeln')
    .factory('Project', project);

/* @ngInject */
function project($resource, FileUploaderCash, PermissionManagement) {
    function transformRequest(data) {
        data = _.extend({}, data);
        data.tags = _.map(data.tags, 'text');
        data.fileIds = _.map(FileUploaderCash.getFiles(), 'id');
        data.accessList = PermissionManagement.expandPermission(data.accessList);

        return data;
    }

    function transformResponse(data) {
        _.each(data.tags, function(tag, i) {
            data.tags[i] = {
                text: tag
            };
        });
        // assetsList is sorted by random on BE
        data.accessList = _.sortBy(data.accessList, 'permissionView');
    }

    return $resource('api/projects/:projectId', {}, {
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
            }
        },
        update: {
            method: 'PUT',
            url: 'api/projects',
            transformRequest: function(data) {
                data = transformRequest(data);

                return angular.toJson(data);
            }
        },
        delete: {
            method: 'DELETE'
        }
    });
}
