angular
    .module('indigoeln')
    .factory('allProjects', function($resource, apiUrl) {
        return $resource(apiUrl + 'projects/all', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    });
