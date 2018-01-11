/* @ngInject */
function autorecoveryCache(CacheFactory, principalService) {
    var cache = CacheFactory('recoveryCache', {
        storageMode: 'localStorage',
        // 24 hours
        maxAge: 86400000
    });

    var visibilityAutorecovery = {};
    var tempRecoveryCache = CacheFactory('tempRecoveryCache');

    var userId = principalService.getUserId();

    principalService.addUserChangeListener(function(id) {
        userId = id;
        visibilityAutorecovery = {};
    });

    return {
        put: put,
        putTempRecoveryData: putTempRecoveryData,
        getTempRecoveryData: getTempRecoveryData,
        removeTempRecoveryData: removeTempRecoveryData,
        isResolved: isResolved,
        isVisible: isVisible,
        tryToVisible: tryToVisible,
        hide: hide,
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

    function putTempRecoveryData(stateParams, data) {
        tempRecoveryCache.put(paramsConverter(stateParams), data);
    }

    function getTempRecoveryData(stateParams) {
        return tempRecoveryCache.get(paramsConverter(stateParams));
    }

    function removeTempRecoveryData(stateParams) {
        tempRecoveryCache.remove(paramsConverter(stateParams));
    }

    function removeByParams(stateParams) {
        cache.remove(paramsConverter(stateParams));
    }

    function clearAll() {
        cache.clearAll();
    }

    function isResolved(stateParams) {
        return !_.isUndefined(visibilityAutorecovery[paramsConverter(stateParams)]);
    }

    function isVisible(stateParams) {
        return visibilityAutorecovery[paramsConverter(stateParams)];
    }

    function tryToVisible(stateParams) {
        if (_.isUndefined(visibilityAutorecovery[paramsConverter(stateParams)])) {
            visibilityAutorecovery[paramsConverter(stateParams)] = true;
        }
    }

    function hide(stateParams) {
        visibilityAutorecovery[paramsConverter(stateParams)] = false;
    }

    function paramsConverter(stateParams) {
        return userId + angular.toJson(stateParams);
    }
}

module.exports = autorecoveryCache;
