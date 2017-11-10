/* @ngInject */
function templateService($resource, apiUrl) {
    return $resource(apiUrl + 'templates/:id', {}, {
        query: {
            method: 'GET', isArray: true
        },
        get: {
            method: 'GET',
            transformResponse: angular.fromJson
        },
        update: {
            method: 'PUT'
        }
    });
}

module.exports = templateService;
