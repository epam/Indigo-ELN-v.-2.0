/* @ngInject */
function roleService($resource, apiUrl) {
    return $resource(apiUrl + 'roles/:id', {}, {
        query: {
            method: 'GET',
            isArray: true
        },
        get: {
            method: 'GET'
        },
        save: {
            method: 'POST'
        },
        update: {
            method: 'PUT'
        },
        delete: {
            method: 'DELETE'
        }
    });
}

module.exports = roleService;
