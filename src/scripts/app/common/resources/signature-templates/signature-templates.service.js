/* @ngInject */
function signatureTemplates($resource, apiUrl) {
        return $resource(apiUrl + 'signature/template', {}, {
            query: {
                method: 'GET'
            }
        });
}

module.exports = signatureTemplates;
