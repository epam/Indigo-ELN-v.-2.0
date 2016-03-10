'use strict';

angular.module('indigoeln')
    .factory('PdfService', function ($resource) {
        return $resource('api/print', {}, {
                'create': {method: 'POST'}
            });
    });
