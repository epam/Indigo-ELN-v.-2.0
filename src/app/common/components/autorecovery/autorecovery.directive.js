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
    function AutoRecoveryController($stateParams, autorecoveryCacheService) {
        var vm = this;

        var recoveryData;
        var tempRecoveryData;

        init();

        function init() {
            recoveryData = autorecoveryCacheService.get($stateParams);
            tempRecoveryData = autorecoveryCacheService.getTempRecoveryData($stateParams);

            if (recoveryData && !tempRecoveryData) {
                autorecoveryCacheService.tryToVisible($stateParams);
                autorecoveryCacheService.putTempRecoveryData($stateParams, recoveryData);
            }

            if (!recoveryData && !autorecoveryCacheService.isVisible($stateParams)) {
                autorecoveryCacheService.hide($stateParams);
            }

            vm.isVisible = (tempRecoveryData || recoveryData) && autorecoveryCacheService.isVisible($stateParams);

            vm.restore = restore;
            vm.remove = remove;
        }

        function restore() {
            vm.onRestore({recoveryData: tempRecoveryData || recoveryData});
            remove();
        }

        function remove() {
            autorecoveryCacheService.hide($stateParams);
            autorecoveryCacheService.remove($stateParams);
            autorecoveryCacheService.removeTempRecoveryData($stateParams);
            recoveryData = null;
            tempRecoveryData = null;
            vm.isVisible = false;
        }
    }
}

module.exports = autorecovery;
