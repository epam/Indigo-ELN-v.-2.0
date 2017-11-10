/* @ngInject */
function projectsForSubCreation($resource, apiUrl) {
    return $resource(apiUrl + 'projects/sub-creations', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = projectsForSubCreation;
