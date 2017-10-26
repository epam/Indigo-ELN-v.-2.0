angular.module('indigoeln')
    .factory('AllProjects', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/all', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('AllNotebooks', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/:projectId/notebooks/all', {
            projectId: '@projectId'
        }, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('AllExperiments', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/:projectId/notebooks/:notebookId/experiments/all',
            {
                projectId: '@projectId',
                notebookId: '@notebookId'
            }, {
                query: {
                    method: 'GET', isArray: true
                }
            });
    });
