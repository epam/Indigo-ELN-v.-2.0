angular
    .module('indigoeln')
    .factory('ProjectsForSubCreation', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/sub-creations', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
