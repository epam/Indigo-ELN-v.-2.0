/* @ngInject */
function notebooksForSubCreationService($resource, apiUrl) {
    return $resource(apiUrl + 'notebooks/sub-creations', {
        projectId: '@projectId'
    }, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = notebooksForSubCreationService;
