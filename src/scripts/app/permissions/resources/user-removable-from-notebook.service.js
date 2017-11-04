/* @ngInject */
function userRemovableFromNotebook($resource, apiUrl) {
    return $resource(apiUrl + 'notebooks/permissions/user-removable', {}, {
        get: {
            method: 'GET'
        }
    });
}

module.exports = userRemovableFromNotebook;
