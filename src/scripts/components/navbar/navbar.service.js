angular.module('indigoeln')
    .factory('ProjectsForSubCreation', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/sub-creations', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('NotebooksForSubCreation', function($resource, apiUrl) {
        return $resource(apiUrl + 'notebooks/sub-creations', {
            projectId: '@projectId'
        }, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
