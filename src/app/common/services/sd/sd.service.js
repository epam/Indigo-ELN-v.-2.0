/* @ngInject */
function sdService($resource, apiUrl) {
    return $resource(apiUrl + 'sd', {}, {
        export: {
            url: apiUrl + 'sd/export', method: 'POST'
        }
    });
}

module.exports = sdService;
