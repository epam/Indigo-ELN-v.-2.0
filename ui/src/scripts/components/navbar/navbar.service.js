angular.module('indigoeln')
    .factory('ProjectsForSubCreation', function($resource) {
        return $resource('api/projects/sub-creations', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('NotebooksForSubCreation', function($resource) {
        return $resource('api/notebooks/sub-creations', {
            projectId: '@projectId'
        }, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
