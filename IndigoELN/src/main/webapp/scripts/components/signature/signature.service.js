angular.module('indigoeln')
    .factory('SignatureTemplates', function ($resource) {
        return $resource('api/signature/template', {}, {
            'query': {method: 'GET'}
        });
    })
    .factory('SignatureDocument', function ($resource) {
        return $resource('api/signature/document', {}, {
            'upload': {method: 'POST'}
        });
    });


