angular
    .module('indigoeln')
    .factory('dashboardService', dashboardService);

dashboardService.$inject = ['$resource', 'apiUrl'];

function dashboardService($resource, apiUrl) {
    return $resource(apiUrl + 'dashboard', {}, {
        get: {
            method: 'GET'
        }
    });
}
