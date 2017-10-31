(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoInlineLoader', indigoInlineLoader);

    function indigoInlineLoader() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/inline-loader/inline-loader.html',
            scope: {
                promise: '=',
                onStatusChanged: '&'
            },
            controller: indigoInlineLoaderController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function indigoInlineLoaderController($scope) {
        var vm = this;

        init();

        function init() {
            bindEvents();
        }

        function bindEvents() {
            $scope.$watch('vm.promise.$$state', function(val) {
                vm.isLoading = (val && val.status === 0);
                if (vm.onStatusChanged) {
                    vm.onStatusChanged({completed: !vm.isLoading});
                }
            }, true);
        }
    }
})();
