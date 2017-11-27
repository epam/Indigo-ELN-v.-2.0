/* @ngInject */
function allProjectsService($resource, apiUrl) {
    return $resource(apiUrl + 'projects/all', {}, {
        query: {
            method: 'GET', isArray: true
        }
    });
}

module.exports = allProjectsService;