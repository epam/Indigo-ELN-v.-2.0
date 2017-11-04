/* @ngInject */
function userWithAuthority($resource, apiUrl) {
    return $resource(apiUrl + 'users/permission-management', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = userWithAuthority;
