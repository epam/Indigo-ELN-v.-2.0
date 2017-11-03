angular
    .module('indigoeln')
    .factory('signatureTemplates', function($resource, apiUrl) {
        return $resource(apiUrl + 'signature/template', {}, {
            query: {
                method: 'GET'
            }
        });
    });
