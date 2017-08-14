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
        cache.put(paramsConverter(stateParams), data);
    }

    function get(stateParams) {
        return cache.get(paramsConverter(stateParams));
    }

    function removeByParams(stateParams) {
        cache.remove(paramsConverter(stateParams));
    }

    function clearAll() {
        cache.clearAll();
    }
    
    function paramsConverter(stateParams) {
        return TabKeyUtils.getTabKeyFromParams(_.extend({isAutorecovery: true}, stateParams));
    }
}
