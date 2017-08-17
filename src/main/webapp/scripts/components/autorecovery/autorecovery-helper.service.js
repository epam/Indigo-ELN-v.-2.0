angular
    .module('indigoeln.autorecovery')
    .factory('autorecoveryHelper', autorecoveryHelper);

/* @ngInject */
function autorecoveryHelper(autorecoveryCache) {
    return {
        getUpdateRecoveryDebounce: getUpdateRecoveryDebounce,
        isEntityDirty: isEntityDirty
    };

    function getUpdateRecoveryDebounce($stateParams) {
        return _.debounce(function(storeData, isDirty) {
            if (storeData) {
                if (isDirty) {
                    autorecoveryCache.put($stateParams, storeData);
                } else if (autorecoveryCache.isResolved($stateParams)) {
                    autorecoveryCache.remove($stateParams);
                }
            }
        }, 10);
    }

    function isEntityDirty(newEntity, originalEntity) {
        return originalEntity && !angular.equals(originalEntity, newEntity);
    }
}
