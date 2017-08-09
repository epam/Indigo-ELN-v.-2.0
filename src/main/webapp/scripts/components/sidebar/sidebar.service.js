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
    });
