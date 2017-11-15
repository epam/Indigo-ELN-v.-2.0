/* @ngInject */
function projectsForSubCreationService($resource, apiUrl) {
    return $resource(apiUrl + 'projects/sub-creations', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = projectsForSubCreationService;
