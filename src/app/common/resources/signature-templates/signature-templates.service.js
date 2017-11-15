/* @ngInject */
function signatureTemplatesService($resource, apiUrl) {
    return $resource(apiUrl + 'signature/template', {},
        {
            query: {
                method: 'GET'
            }
        }
    );
}

module.exports = signatureTemplatesService;
