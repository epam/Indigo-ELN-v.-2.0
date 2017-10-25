angular
    .module('indigoeln')
    .factory('notebookSummaryExperiments', function($resource) {
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

        return $resource('api/projects/:projectId/notebooks/:notebookId/experiments/notebook-summary',
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
    });
