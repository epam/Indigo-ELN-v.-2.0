(function() {
    angular
        .module('indigoeln')
        .directive('customScroll', customScroll);

    customScroll.$inject = ['simpleLocalCache', '$timeout'];

    function customScroll(simpleLocalCache, $timeout) {
        return {
            restrict: 'A',
            scope: {
                customScroll: '@'
            },
            link: link,
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true
        };

        function link($scope, $element, $attributes, vm) {
            var scrollLocation = simpleLocalCache.getByKey(vm.customScroll) || 0;
            var el = $element[0];
            var scrollPosition = 0;

            $timeout(function() {
                $element.scrollTop(scrollLocation);
                angular.getTestability($element).whenStable(function() {
                    $element.scrollTop(scrollLocation);
                });
            });

            $element.on('scroll', function() {
                scrollPosition = el.scrollTop;
            });

            $scope.$on('$destroy', function() {
                simpleLocalCache.putByKey(vm.customScroll, scrollPosition);
            });
        }
    }
})();
