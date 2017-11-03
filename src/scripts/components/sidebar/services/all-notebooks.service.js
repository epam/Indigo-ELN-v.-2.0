angular
    .module('indigoeln')
    .factory('allNotebooks', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/:projectId/notebooks/all', {
            projectId: '@projectId'
        }, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
