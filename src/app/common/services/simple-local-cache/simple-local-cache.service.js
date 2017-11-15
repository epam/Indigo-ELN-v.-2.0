/* @ngInject */
function simpleLocalCache(CacheFactory) {
    var cache = CacheFactory('simpleLocalCache', {
        storageMode: 'localStorage'
    });

    return {
        putByKey: putByKey,
        getByKey: getByKey,
        removeByKey: removeByKey
    };

    function putByKey(key, data) {
        cache.put(key, data);
    }

    function getByKey(key) {
        return cache.get(key);
    }

    function removeByKey(key) {
        cache.remove(key);
    }
}

module.exports = simpleLocalCache;
