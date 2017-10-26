angular.module('indigoeln')
    .factory('SignatureTemplates', function($resource, apiUrl) {
        return $resource(apiUrl + 'signature/template', {}, {
            query: {
                method: 'GET'
            }
        });
    })
    .factory('SignatureDocument', function($resource, apiUrl) {
        return $resource(apiUrl + 'signature/document', {}, {
            upload: {
                method: 'POST'
            }
        });
    });

