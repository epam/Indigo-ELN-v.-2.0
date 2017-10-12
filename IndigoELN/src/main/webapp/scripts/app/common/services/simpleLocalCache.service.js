(function() {
    angular.module('indigoeln')
        .factory('simpleLocalCache', simpleLocalCache);

    sidebarCache.$inject = ['CacheFactory', 'Principal'];
    function sidebarCache(CacheFactory) {
        var cache = CacheFactory('sidebarCache', {
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
})();