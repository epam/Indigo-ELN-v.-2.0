angular.module('indigoeln')
    .factory('PdfService', function($resource, apiUrl) {
        return $resource(apiUrl + 'print', {}, {
            create: {
                method: 'POST'
            }
        });
    });
