autorecoveryHelperService.$inject = ['autorecoveryCacheService'];

function autorecoveryHelperService(autorecoveryCacheService) {
    return {
        getUpdateRecoveryDebounce: getUpdateRecoveryDebounce,
        isEntityDirty: isEntityDirty
    };

    function getUpdateRecoveryDebounce($stateParams) {
        return _.debounce(function(storeData, isDirty) {
            if (storeData) {
                if (isDirty) {
                    autorecoveryCacheService.put($stateParams, storeData);
                } else if (autorecoveryCacheService.isResolved($stateParams)) {
                    autorecoveryCacheService.remove($stateParams);
                }
            }
        }, 10);
    }

    function isEntityDirty(newEntity, originalEntity) {
        return originalEntity && !angular.equals(originalEntity, newEntity);
    }
}

module.exports = autorecoveryHelperService;
