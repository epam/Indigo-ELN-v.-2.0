(function() {
    angular.module('indigoeln')
        .factory('sidebarCache', sidebarCache);

    sidebarCache.$inject = ['CacheFactory', 'Principal'];
    function sidebarCache(CacheFactory, Principal) {
        var aDay = 86400000;
        var aMinute = 3600;

        var cache = CacheFactory('sidebarCache', {
            storageMode: 'localStorage',
            maxAge: aDay * 7,
            deleteOnExpire: 'aggressive',
            recycleFreq: aMinute * 5
        });

        return {
            put: put,
            get: get
        };

        function put(path, data) {
            cache.put(Principal.getIdentity().id + path, data);
        }

        function get(path) {
            return cache.get(Principal.getIdentity().id + path);
        }
    }
})();
