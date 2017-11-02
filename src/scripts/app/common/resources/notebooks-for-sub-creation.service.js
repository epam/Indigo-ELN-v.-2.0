angular
    .module('indigoeln')
    .factory('NotebooksForSubCreation', function($resource, apiUrl) {
        return $resource(apiUrl + 'notebooks/sub-creations', {
            projectId: '@projectId'
        }, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
