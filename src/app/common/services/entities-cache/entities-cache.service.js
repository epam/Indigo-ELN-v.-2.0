entitiesCache.$inject = ['CacheFactory', 'tabKeyUtils'];

function entitiesCache(CacheFactory, tabKeyUtils) {
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
        cache.put(tabKeyUtils.getTabKeyFromParams(stateParams), data);
    }

    function get(stateParams) {
        return cache.get(tabKeyUtils.getTabKeyFromParams(stateParams));
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
        cache.remove(tabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function clearAll() {
        cache.removeAll();
    }
}

module.exports = entitiesCache;
