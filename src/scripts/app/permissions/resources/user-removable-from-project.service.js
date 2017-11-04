/* @ngInject */
function userRemovableFromProject($resource, apiUrl) {
    return $resource(apiUrl + 'projects/permissions/user-removable', {}, {
        get: {
            method: 'GET'
        }
    });
}

module.exports = userRemovableFromProject;
