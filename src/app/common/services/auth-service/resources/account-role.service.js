/* @ngInject */
function accountRoleService($resource, apiUrl) {
    return $resource(apiUrl + 'accounts/account/roles', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = accountRoleService;
