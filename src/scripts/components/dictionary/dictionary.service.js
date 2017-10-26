angular.module('indigoeln')
    .factory('Dictionary', function($resource, CacheFactory) {
        var dictionaryCache = CacheFactory.createCache('dictionaryCache', {
            maxAge: 5 * 60 * 1000,
            deleteOnExpire: 'passive'
        });

        return $resource('api/dictionaries/:id', {}, {
            query: {
                method: 'GET',
                isArray: true
            },
            get: {
                method: 'GET'
            },
            all: {
                url: 'api/dictionaries/all',
                method: 'GET',
                isArray: true
            },
            getByName: {
                url: 'api/dictionaries/byName/:name',
                method: 'GET',
                cache: dictionaryCache
            },
            save: {
                method: 'POST'
            },
            update: {
                method: 'PUT'
            },
            delete: {
                method: 'DELETE'
            }
        });
    });
