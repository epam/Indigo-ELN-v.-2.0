angular.module('indigoeln')
    .factory('Experiment', function ($resource) {
        function toModel(experiment) {
            var components = experiment.components;
            if (_.isArray(components)) {
                experiment.components = _.object(_.map(components, function (component) {
                    return [component.name, component.content];
                }));
                return experiment;
            } else {
                return experiment;
            }
        }
        function toComponents(model) {
            return _.map(model, function (val, key) {
                return {name: key, content: val};
            });
        }
        return $resource('api/projects/:projectId/notebooks/:notebookId/experiments/:experimentId',
            {
                projectId: '@projectId',
                notebookId: '@notebookId',
                experimentId: '@experimentId'
            }, {
                'query': {method: 'GET', isArray: true,
                    transformResponse: function (data) {
                        data = angular.fromJson(data);
                        _.each(data, function(item, key){
                            data[key] = toModel(item);
                        });
                        return data;
                    }},
                'get': {
                    method: 'GET',
                    transformResponse: function (data) {
                        data = angular.fromJson(data);
                        data = toModel(data);
                        return data;
                    }
                },
                'save': {method: 'POST',
                    transformRequest: function (data) {
                        data.components = toComponents(data.components);
                        return angular.toJson(data);
                    }
                },
                'version': {
                    method: 'POST',
                    url: 'api/projects/:projectId/notebooks/:notebookId/experiments/:experimentId/version'
                },
                'update': {method: 'PUT',
                    transformRequest: function (data) {
                        data.components = toComponents(data.components);
                        return angular.toJson(data);
                    }
                },
                'delete': {method: 'DELETE'}
            });
    });
