angular.module('indigoeln')
    .factory('AllProjects', function($resource) {
        return $resource('api/projects/all', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('AllNotebooks', function($resource) {
        return $resource('api/projects/:projectId/notebooks/all', {
            projectId: '@projectId'
        }, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('AllExperiments', function($resource) {
        return $resource('api/projects/:projectId/notebooks/:notebookId/experiments/all',
            {
                projectId: '@projectId',
                notebookId: '@notebookId'
            }, {
                query: {
                    method: 'GET', isArray: true
                }
            });
    })
    .factory('NotebookSummaryExperiments', function($resource) {
        function toModel(experiment) {
            var components = experiment.components;
            if (_.isArray(components)) {
                experiment.components = _.values(_.map(components, function(component) {
                    return [component.name, component.content];
                }));

                return experiment;
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
                        data = angular.fromJson(data);
                        _.each(data, function(item, key) {
                            data[key] = toModel(item);
                        });

                        return data;
                    }
                }
            });
    });
