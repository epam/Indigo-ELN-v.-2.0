(function() {
    angular
        .module('indigoeln.autorecovery')
        .directive('autorecovery', autorecoveryDirective);

    function autorecoveryDirective() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/autorecovery/autorecovery.html',
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
            init();

            function init() {
                vm.isResolved = false;
                vm.isVisible = false;

                recoveryData = autorecoveryCache.get($stateParams);
                vm.isVisible = !!recoveryData;

                vm.restore = restore;
                vm.remove = remove;
            }

            function restore() {
                vm.onRestore({recoveryData: recoveryData});
                vm.isResolved = true;
            }

            function remove() {
                autorecoveryCache.remove($stateParams);
                recoveryData = null;
                vm.isResolved = true;
            }
        }
    }
})();
