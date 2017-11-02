angular
    .module('indigoeln')
    .factory('SignatureDocument', function($resource, apiUrl) {
        return $resource(apiUrl + 'signature/document', {}, {
            upload: {
                method: 'POST'
            }
        });
    });

