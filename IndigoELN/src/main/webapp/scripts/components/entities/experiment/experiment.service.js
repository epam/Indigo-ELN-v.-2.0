angular
    .module('indigoeln')
    .factory('Experiment', experiment);

/* @ngInject */
function experiment($resource, PermissionManagement, entityTreeService) {
    var interceptor = {
        response: function(response) {
            entityTreeService.updateExperiment(response.data);

            return response.data;
        }
    };

    return $resource('api/projects/:projectId/notebooks/:notebookId/experiments/:experimentId',
        {
            projectId: '@projectId',
            notebookId: '@notebookId',
            experimentId: '@experimentId'
        }, {
            query: {
                method: 'GET',
                isArray: true,
                transformResponse: function(data) {
                    data = angular.fromJson(data);
                    _.each(data, function(item, key) {
                        data[key] = toModel(item);
                    });

                    return data;
                }
            },
            get: {
                method: 'GET',
                transformResponse: transformResponse,
                interceptor: interceptor
            },
            save: {
                method: 'POST',
                transformRequest: function(data) {
                    data = transformRequest(data);

                    return angular.toJson(data);
                },
                transformResponse: transformResponse,
                interceptor: interceptor
            },
            version: {
                method: 'POST',
                url: 'api/projects/:projectId/notebooks/:notebookId/experiments/:experimentId/version',
                interceptor: interceptor
            },
            update: {
                method: 'PUT',
                url: 'api/projects/:projectId/notebooks/:notebookId/experiments',
                transformRequest: function(data) {
                    data = transformRequest(data);

                    return angular.toJson(data);
                },
                transformResponse: transformResponse,
                interceptor: interceptor
            },
            delete: {
                method: 'DELETE'
            },
            print: {
                method: 'GET',
                url: 'api/print/project/:projectId/notebook/:notebookId/experiment/:experimentId'
            },
            reopen: {
                method: 'PUT',
                url: 'api/projects/:projectId/notebooks/:notebookId/experiments/:experimentId/reopen',
                interceptor: interceptor
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
        data = _.extend({}, data);
        data.components = toComponents(data.components);
        data.accessList = PermissionManagement.expandPermission(data.accessList);

        return data;
    }

    function transformResponse(data) {
        data = angular.fromJson(data);
        data = toModel(data);
        data.creationDate = moment(data.creationDate).toISOString();

        return data;
    }
}
