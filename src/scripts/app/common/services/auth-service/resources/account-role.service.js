/* @ngInject */
function accountRole($resource, apiUrl) {
    return $resource(apiUrl + 'accounts/account/roles', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = accountRole;
