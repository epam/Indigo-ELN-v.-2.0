angular
    .module('indigoeln.sidebarModule')
    .factory('allExperiments', function($resource, apiUrl) {
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
