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
