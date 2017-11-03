angular
    .module('indigoeln')
    .factory('projectsForSubCreation', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/sub-creations', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
