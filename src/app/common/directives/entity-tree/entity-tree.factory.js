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
function entityTreeFactory(apiUrl, $http) {
    return {
        getProjects: function(isAll) {
            var url = isAll ? 'tree/projects/all' : 'tree/projects';

            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getProject: function(projectId) {
            var url = 'tree/projects/' + projectId;

            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getNotebooks: function(projectId, isAll) {
            var url = 'tree/projects/' + projectId + '/notebooks';
            if (isAll) {
                url += '/all';
            }

            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getNotebook: function(projectId, notebookId) {
            var url = 'tree/projects/' + projectId + '/notebooks/' + notebookId;

            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getExperiments: function(projectId, notebookId, isAll) {
            var url = 'tree/projects/' + projectId + '/notebooks/' + notebookId + '/experiments';
            if (isAll) {
                url += '/all';
            }

            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getExperiment: function(projectId, notebookId, experimentId) {
            var url = 'tree/projects/' + projectId + '/notebooks/' + notebookId + '/experiments/' + experimentId;

            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        }
    };
}

module.exports = entityTreeFactory;
