(function() {
    angular
        .module('indigoeln')
        .directive('indigoTabScroller', indigoTabScroller);

    /* @ngInject */
    function indigoTabScroller($timeout) {
        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link(scope, iElement) {
            $timeout(function() {
                var $element = $(iElement);
                $element.mCustomScrollbar({
                    axis: 'x',
                    theme: 'indigo',
                    scrollInertia: 100
                });
                scope.$watch('vm.activeTab', function() {
                    $element.mCustomScrollbar('update');
                    $timeout(function() {
                        var l = $element.find('.active').position().left;
                        $element.mCustomScrollbar('scrollTo', l, {
                            scrollInertia: 300
                        });
                    }, 100);
                });
            }, 0, false);
        }
    }
})();
