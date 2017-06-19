angular
    .module('indigoeln')
    .factory('EntitiesCache', entitiesCache);

/* @ngInject */
function entitiesCache(CacheFactory, TabKeyUtils) {
    var entitiesCache = CacheFactory('entitiesCache');

    return {
        put: put,
        get: get,
        getByKey: getByKey,
        removeByKey: removeByKey,
        removeByParams: removeByParams,
        clearAll: clearAll
    };

    function put(stateParams, data) {
        entitiesCache.put(TabKeyUtils.getTabKeyFromParams(stateParams), data);
    }

    function get(stateParams) {
        return entitiesCache.get(TabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function getByKey(key) {
        return entitiesCache.get(key);
    }

    function removeByKey(key) {
        entitiesCache.remove(key);
    }

    function removeByParams(stateParams) {
        entitiesCache.remove(TabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function clearAll() {
        CacheFactory.clearAll();
    }
}
