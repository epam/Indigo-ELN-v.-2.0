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
function notebookSummaryExperimentsService($resource, apiUrl) {
    function toModel(experiment) {
        if (_.isArray(experiment.components)) {
            var components = {};
            _.each(experiment.components, function(component) {
                components[component.name] = component.content;
            });
            experiment.components = components;
        }

        return experiment;
    }

    return $resource(apiUrl + 'projects/:projectId/notebooks/:notebookId/experiments/notebook-summary',
        {
            projectId: '@projectId',
            notebookId: '@notebookId'
        }, {
            query: {
                method: 'GET',
                isArray: true,
                transformResponse: function(data) {
                    return _.map(angular.fromJson(data), toModel);
                }
            }
        });
}

module.exports = notebookSummaryExperimentsService;
