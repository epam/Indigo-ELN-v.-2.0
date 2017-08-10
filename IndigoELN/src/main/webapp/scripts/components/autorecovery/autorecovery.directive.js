(function() {
    angular
        .module('indigoeln.autorecovery')
        .directive('autorecovery', indigoAutorecovery);

    function indigoAutorecovery() {
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
        function AutoRecoveryController($stateParams, recoveryCacheFactory) {
            var vm = this;

            var recoveryData;
            init();

            function init() {
                vm.isResolved = false;
                vm.isVisible = false;

                recoveryData = recoveryCacheFactory.get($stateParams);

                if (recoveryData) {
                    vm.isVisible = true;
                }

                vm.restore = function() {
                    vm.onRestore({recoveryData: recoveryData});
                    vm.isResolved = true;
                };
            }
        }
    }
})();
