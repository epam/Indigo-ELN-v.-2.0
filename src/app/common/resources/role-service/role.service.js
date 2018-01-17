/* @ngInject */
function roleService($resource, apiUrl) {
    return $resource(apiUrl + 'roles/:id', {}, {
        query: {
            method: 'GET',
            transformResponse: function(data, headersGetter) {
                return {
                    data: angular.fromJson(data),
                    totalItemsCount: headersGetter('x-total-count')
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
