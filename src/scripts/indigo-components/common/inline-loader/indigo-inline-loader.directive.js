(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoInlineLoader', indigoInlineLoader);

    function indigoInlineLoader() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/inline-loader/inline-loader.html',
            scope: {
                promise: '=',
                onStatusChanged: '&'
            },
            controller: IndigoInlineLoaderController,
            controllerAs: 'vm',
            bindToController: true
        };
    }

    /* @ngInject */
    function IndigoInlineLoaderController($scope) {
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
