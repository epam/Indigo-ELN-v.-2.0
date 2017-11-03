angular
    .module('indigoeln')
    .factory('entitiesCache', entitiesCacheFactory);

/* @ngInject */
function entitiesCacheFactory(CacheFactory, tabKeyUtils) {
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
        entitiesCache.put(tabKeyUtils.getTabKeyFromParams(stateParams), data);
    }

    function putByName(name, data) {
        entitiesCache.put(name, data);
    }

    function getByName(name) {
       return entitiesCache.get(name);
    }

    function get(stateParams) {
        return entitiesCache.get(tabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function getByKey(key) {
        return entitiesCache.get(key);
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
