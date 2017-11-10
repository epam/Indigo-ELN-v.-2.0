/* @ngInject */
function dictionaryService($resource, CacheFactory, apiUrl) {
    var dictionaryCache = CacheFactory.createCache('dictionaryCache', {
        maxAge: 5 * 60 * 1000,
        deleteOnExpire: 'passive'
    });

    return $resource(apiUrl + 'dictionaries/:id', {}, {
        query: {
            method: 'GET',
            isArray: true
        },
        get: {
            method: 'GET'
        },
        all: {
            url: apiUrl + 'dictionaries/all',
            method: 'GET',
            isArray: true
        },
        getByName: {
            url: apiUrl + 'dictionaries/byName/:name',
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
}

module.exports = dictionaryService;
