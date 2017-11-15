/* @ngInject */
function userWithAuthorityService($resource, apiUrl) {
    return $resource(apiUrl + 'users/permission-management', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = userWithAuthorityService;
