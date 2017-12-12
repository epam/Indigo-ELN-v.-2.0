/* @ngInject */
function userPasswordValidationService($resource, apiUrl) {
    return $resource(apiUrl + 'users/passwordValidationRegex', {}, {
        get: {
            method: 'GET',
            cache: true,
            transformResponse: function(regex) {
                return {data: regex};
            }
        }
    });
}

module.exports = userPasswordValidationService;
