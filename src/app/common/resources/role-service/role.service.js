/* @ngInject */
function roleService($resource, apiUrl) {
    return $resource(apiUrl + 'roles/:id', {}, {
        query: {
            method: 'GET',
            transformResponse: function(data, headersGetter) {
                return {
                    data: angular.fromJson(data),
                    headers: headersGetter()
                };
            }
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
