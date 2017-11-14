/* @ngInject */
function allNotebooksService($resource, apiUrl) {
    return $resource(apiUrl + 'projects/:projectId/notebooks/all', {
        projectId: '@projectId'
    }, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = allNotebooksService;
