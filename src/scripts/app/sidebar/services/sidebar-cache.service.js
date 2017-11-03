(function() {
    angular
        .module('indigoeln.sidebarModule')
        .factory('sidebarCache', sidebarCache);

    sidebarCache.$inject = ['CacheFactory', 'principalService'];
    function sidebarCache(CacheFactory, principalService) {
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
            cache.put(principalService.getIdentity().id + path, data);
        }

        function get(path) {
            return cache.get(principalService.getIdentity().id + path);
        }
    }
})();
