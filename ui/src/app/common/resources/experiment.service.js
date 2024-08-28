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

var moment = require('moment');

/* @ngInject */
function experimentService($resource, permissionService, apiUrl, entityTreeService) {
    return $resource(apiUrl + 'projects/:projectId/notebooks/:notebookId/experiments/:experimentId',
        {
            projectId: '@projectId',
            notebookId: '@notebookId',
            experimentId: '@experimentId'
        }, {
            get: {
                method: 'GET',
                transformResponse: function(data) {
                    var experiment = transformResponse(data);
                    entityTreeService.updateExperimentByEntity(experiment);

                    return experiment;
                }
            },
            save: {
                method: 'POST',
                transformRequest: transformRequest,
                transformResponse: function(data) {
                    var experiment = transformResponse(data);
                    entityTreeService.addExperimentByEntity(experiment);

                    return experiment;
                }
            },
            version: {
                method: 'POST',
                url: apiUrl + 'projects/:projectId/notebooks/:notebookId/experiments/:experimentId/version',
                transformResponse: function(data) {
                    var experiment = transformResponse(data);
                    entityTreeService.addExperimentByEntity(experiment);

                    return experiment;
                }
            },
            update: {
                method: 'PUT',
                url: apiUrl + 'projects/:projectId/notebooks/:notebookId/experiments',
                transformRequest: transformRequest,
                transformResponse: function(data) {
                    var experiment = transformResponse(data);
                    entityTreeService.updateExperimentByEntity(experiment);

                    return experiment;
                }
            },
            delete: {method: 'DELETE'},
            print: {
                method: 'GET',
                url: apiUrl + 'print/project/:projectId/notebook/:notebookId/experiment/:experimentId'
            },
            reopen: {
                method: 'PUT',
                url: apiUrl + 'projects/:projectId/notebooks/:notebookId/experiments/:experimentId/reopen'
            }
        });

    function toModel(experiment) {
        var components = experiment.components;
        if (_.isArray(components)) {
            experiment.components = {};

            _.forEach(components, function(component) {
                experiment.components[component.name] = component.content;
            });

            return experiment;
        }

        return experiment;
    }

    function toComponents(model) {
        return _.map(model, function(val, key) {
            return {
                name: key, content: val
            };
        });
    }

    function transformRequest(data) {
        var copiedRequest = angular.copy(data);
        copiedRequest.components = toComponents(copiedRequest.components);
        copiedRequest.accessList = permissionService.expandPermission(copiedRequest.accessList);

        return angular.toJson(copiedRequest);
    }

    function transformResponse(data) {
        var experiment = toModel(angular.fromJson(data));

        experiment.creationDate = moment(experiment.creationDate).toISOString();

        if (!_.get(experiment, 'components.stoichTable.products')) {
            return experiment;
        }
        _.forEach(experiment.components.stoichTable.products, function(batch) {
            if (!batch.$$batchHash) {
                batch.$$batchHash = batch.formula.value + batch.exactMass;
            }
        });

        return experiment;
    }
}

module.exports = experimentService;
