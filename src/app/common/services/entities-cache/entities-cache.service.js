entitiesCache.$inject = ['CacheFactory', 'tabKeyService'];

function entitiesCache(CacheFactory, tabKeyService) {
    var cache = CacheFactory('entitiesCache');

    return {
        put: put,
        get: get,
        getByKey: getByKey,
        putByKey: putByKey,
        removeByKey: removeByKey,
        removeByParams: removeByParams,
        clearAll: clearAll
    };

    function put(stateParams, data) {
        cache.put(tabKeyService.getTabKeyFromParams(stateParams), data);
    }

    function get(stateParams) {
        return cache.get(tabKeyService.getTabKeyFromParams(stateParams));
    }

    function getByKey(key) {
        return cache.get(key);
    }

    function putByKey(key, data) {
        cache.put(key, data);
    }

    function removeByKey(key) {
        cache.remove(key);
    }

    function removeByParams(stateParams) {
        cache.remove(tabKeyService.getTabKeyFromParams(stateParams));
    }

    function clearAll() {
        cache.removeAll();
    }
}

module.exports = entitiesCache;
