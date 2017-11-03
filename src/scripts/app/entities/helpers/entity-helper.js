(function() {
    angular
        .module('indigoeln')
        .factory('entityHelper', entityHelper);

    /* @ngInject */
    function entityHelper(CacheFactory, tabKeyUtils, confirmationModal, notifyService) {
        var versionCache = CacheFactory('versionCache');
        var isConflictConfirmOpen = false;

        return {
            checkVersion: checkVersion
        };

        function checkVersion($stateParams, data, currentEntity, entityTitle, isChanged, refreshClbk) {
            if (_.isEqual($stateParams, data.entity) && data.version > currentEntity.version) {
                versionCache.put(tabKeyUtils.getTabKeyFromParams($stateParams), data.version);

                if (isChanged) {
                    if (isConflictConfirmOpen) {
                        return;
                    }

                    isConflictConfirmOpen = confirmationModal
                        .openEntityVersionsConflictConfirm(entityTitle)
                        .then(refreshClbk, function() {
                            currentEntity.version = versionCache.get(tabKeyUtils.getTabKeyFromParams($stateParams));
                        })
                        .finally(function() {
                            isConflictConfirmOpen = false;
                        });

                    return;
                }
                refreshClbk().then(function() {
                    notifyService.info(entityTitle + ' has been changed by another user and reloaded');
                });
            }
        }
    }
})();
