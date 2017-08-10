angular
    .module('indigoeln.autorecovery')
    .factory('autorecoveryCache', autorecoveryCacheFactory);

/* @ngInject */
function autorecoveryCacheFactory(CacheFactory, TabKeyUtils) {
    var cache = CacheFactory('recoveryCache', {
        storageMode: 'localStorage'
    });

    return {
        put: put,
        get: get,
        remove: removeByParams,
        clearAll: clearAll
    };

    function put(stateParams, data) {
        cache.put(TabKeyUtils.getTabKeyFromParams(stateParams), data);
    }

    function get(stateParams) {
        return cache.get(TabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function removeByParams(stateParams) {
        cache.remove(TabKeyUtils.getTabKeyFromParams(stateParams));
    }

    function clearAll() {
        cache.clearAll();
    }
}
