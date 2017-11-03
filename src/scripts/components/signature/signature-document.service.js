angular
    .module('indigoeln')
    .factory('signatureDocument', function($resource, apiUrl) {
        return $resource(apiUrl + 'signature/document', {}, {
            upload: {
                method: 'POST'
            }
        });
    });

