angular
    .module('indigoeln')
    .factory('EntitiesCache', entitiesCacheFactory);

/* @ngInject */
function entitiesCacheFactory(CacheFactory, TabKeyUtils) {
    var entitiesCache = CacheFactory('entitiesCache');

    return {
        put: put,
        putByName: putByName,
        getByName: getByName,
        get: get,
        getByKey: getByKey,
        removeByKey: removeByKey,
        removeByParams: removeByParams,
        clearAll: clearAll
    };

    function put(stateParams, data) {
        entitiesCache.put(TabKeyUtils.getTabKeyFromParams(stateParams), data);
    }

    function putByName(name, data) {
        entitiesCache.put(name, data);
    }

    function getByName(name) {
       return entitiesCache.get(name);
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
        entitiesCache.removeAll();
    }
}
