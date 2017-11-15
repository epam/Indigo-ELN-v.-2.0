/* @ngInject */
function userRemovableFromNotebookService($resource, apiUrl) {
    return $resource(apiUrl + 'notebooks/permissions/user-removable', {}, {
        get: {
            method: 'GET'
        }
    });
}

module.exports = userRemovableFromNotebookService;
