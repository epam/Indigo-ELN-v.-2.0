/* @ngInject */
function entitiesCacheService(CacheFactory, tabKeyUtils) {
    var entitiesCache = CacheFactory('entitiesCache');

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
        entitiesCache.put(tabKeyUtils.getTabKeyFromParams(stateParams), data);
    }

    function get(stateParams) {
        return entitiesCache.get(tabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function getByKey(key) {
        return entitiesCache.get(key);
    }

    function putByKey(key, data) {
        entitiesCache.put(key, data);
    }

    function removeByKey(key) {
        entitiesCache.remove(key);
    }

    function removeByParams(stateParams) {
        entitiesCache.remove(tabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function clearAll() {
        entitiesCache.removeAll();
    }
}

module.exports = entitiesCacheService;
