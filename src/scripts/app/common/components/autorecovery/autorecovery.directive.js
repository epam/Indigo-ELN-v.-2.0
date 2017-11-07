var template = require('./autorecovery.html');

function autorecovery() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            kind: '@',
            name: '@',
            onRestore: '&'
        },
        controller: AutoRecoveryController,
        bindToController: true,
        controllerAs: 'vm'
    };

    /* @ngInject */
    function AutoRecoveryController($stateParams, autorecoveryCache) {
        var vm = this;

        var recoveryData;
        var tempRecoveryData;

        init();

        function init() {
            recoveryData = autorecoveryCache.get($stateParams);
            tempRecoveryData = autorecoveryCache.getTempRecoveryData($stateParams);

            if (recoveryData && !tempRecoveryData) {
                autorecoveryCache.tryToVisible($stateParams);
                autorecoveryCache.putTempRecoveryData($stateParams, recoveryData);
            }

            if (!recoveryData && !autorecoveryCache.isVisible($stateParams)) {
                autorecoveryCache.hide($stateParams);
            }

            vm.isVisible = (tempRecoveryData || recoveryData) && autorecoveryCache.isVisible($stateParams);

            vm.restore = restore;
            vm.remove = remove;
        }

        function restore() {
            vm.onRestore({recoveryData: tempRecoveryData || recoveryData});
            remove();
        }

        function remove() {
            autorecoveryCache.hide($stateParams);
            autorecoveryCache.remove($stateParams);
            autorecoveryCache.removeTempRecoveryData($stateParams);
            recoveryData = null;
            tempRecoveryData = null;
            vm.isVisible = false;
        }
    }
}

module.export = autorecovery;
