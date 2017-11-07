/* @ngInject */
function notebooksForSubCreation($resource, apiUrl) {
    return $resource(apiUrl + 'notebooks/sub-creations', {
        projectId: '@projectId'
    }, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = notebooksForSubCreation;
