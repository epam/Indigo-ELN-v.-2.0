/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function projectService($resource, fileUploader, permissionService, apiUrl, entityTreeService) {
    function transformRequest(data) {
        var newData = angular.copy(data);
        newData.tags = _.map(newData.tags, 'text');
        newData.fileIds = _.map(fileUploader.getFiles(), 'id');
        newData.accessList = permissionService.expandPermission(newData.accessList);

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
        get: {
            method: 'GET',
            transformResponse: transformResponse
        },
        save: {
            method: 'POST',
            transformRequest: transformRequest,
            transformResponse: function(data) {
                var proj = transformResponse(data);
                entityTreeService.addProject(proj);

                return proj;
            }
        },
        update: {
            method: 'PUT',
            url: apiUrl + 'projects',
            transformRequest: transformRequest,
            transformResponse: function(data) {
                var proj = transformResponse(data);
                entityTreeService.updateProjectByEntity(proj);

                return proj;
            }
        },
        delete: {method: 'DELETE'},
        print: {
            method: 'GET',
            url: apiUrl + 'print/project/:projectId'
        }
    });
}

module.exports = projectService;
