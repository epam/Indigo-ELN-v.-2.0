angular.module('indigoeln')
    .factory('SearchService', function ($resource) {
        return $resource('api/search', {}, {
            'getCatalogues': {url: 'api/search/catalogue', method: 'GET', isArray: true},
            'search': {url: 'api/search/batch', method: 'POST', isArray: true}
        });
    });


