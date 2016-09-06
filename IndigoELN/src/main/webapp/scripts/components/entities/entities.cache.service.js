angular.module('indigoeln')
    .factory('EntitiesCache', function (CacheFactory, TabKeyUtils) {
        var entitiesCache = CacheFactory('entitiesCache');

        return {
            put: function (stateParams, data) {
                entitiesCache.put(TabKeyUtils.getTabKeyFromParams(stateParams), data);
            },

            get: function (stateParams) {
                return entitiesCache.get(TabKeyUtils.getTabKeyFromParams(stateParams));
            },

            getByKey: function (key) {
                return entitiesCache.get(key);
            },

            removeByKey: function (key) {
                entitiesCache.remove(key);
            },

            removeByParams: function (stateParams) {
                entitiesCache.remove(TabKeyUtils.getTabKeyFromParams(stateParams));
            },

            clearAll: function () {
                CacheFactory.clearAll();
            }
        }
    });