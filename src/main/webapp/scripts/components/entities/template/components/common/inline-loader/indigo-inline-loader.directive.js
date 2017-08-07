(function() {
    angular
        .module('indigoeln')
        .directive('indigoInlineLoader', indigoInlineLoader);

    function indigoInlineLoader() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/inline-loader/inline-loader.html',
            scope: {
                promise: '=',
                isLoading: '='
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
            }, true);
        }
    }
})();
