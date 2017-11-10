/* @ngInject */
function signatureDocument($resource, apiUrl) {
    return $resource(apiUrl + 'signature/document', {}, {
    upload: {
        method: 'POST'
    }
});
}

module.exports = signatureDocument;

