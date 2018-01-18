/* @ngInject */
function searchService($resource, apiUrl) {
    return $resource(apiUrl + 'search', {}, {
        getExperiments: {
            url: apiUrl + 'search/experiments', method: 'GET', isArray: true
        },
        getCatalogues: {
            url: apiUrl + 'search/catalogue', method: 'GET', isArray: true
        },
        search: {
            url: apiUrl + 'search/batch', method: 'POST', isArray: true
        },
        searchAll: {
            url: apiUrl + 'search', method: 'POST', isArray: true
        }
    });
}

module.exports = searchService;
