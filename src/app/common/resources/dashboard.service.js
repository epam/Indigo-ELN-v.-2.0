/* @ngInject */
function dashboardService($resource, apiUrl) {
    return $resource(apiUrl + 'dashboard', {}, {
        get: {
            method: 'GET'
        }
    });
}

module.exports = dashboardService;
