/* @ngInject */
function userReagentsService($resource, apiUrl) {
    return $resource(apiUrl + 'user_reagents', {}, {
        get: {
            method: 'GET', isArray: true
        },
        save: {
            method: 'POST'
        }
    });
}

module.exports = userReagentsService;
