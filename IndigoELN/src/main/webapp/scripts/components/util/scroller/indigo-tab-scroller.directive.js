(function () {
    angular
        .module('indigoeln')
        .directive('indigoTabScroller', indigoTabScroller);

    /* @ngInject */
    function indigoTabScroller($timeout) {
        return {
            restrict: 'A',
            link: angular.bind({$timeout: $timeout}, link)
        };
    }
    
    /* @ngInject */
    function link(scope, iElement) {
        var $timeout = this.$timeout;

        $timeout(function () {
            var $element = $(iElement);
            $element.mCustomScrollbar({
                axis: 'x',
                theme: 'indigo',
                scrollInertia: 100
            });
            var onTabChanged = scope.$watch('activeTab', function () {
                $element.mCustomScrollbar('update');
                $timeout(function () {
                    var l = $element.find('.active').position().left;
                    $element.mCustomScrollbar('scrollTo', l, {
                        scrollInertia: 300
                    });
                }, 100);
            });
            scope.$on('$destroy', function () {
                onTabChanged();
            });
        }, 0, false);
    }
})();