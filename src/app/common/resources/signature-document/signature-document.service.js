/* @ngInject */
function signatureDocumentService($resource, apiUrl) {
    return $resource(apiUrl + 'signature/document', {},
        {
            upload: {
                method: 'POST'
            }
        }
    );
}

module.exports = signatureDocumentService;

