indigoTabScroller.$inject = ['$timeout'];

function indigoTabScroller($timeout) {
    return {
        restrict: 'A',
        link: link
    };

    /* @ngInject */
    function link(scope, $element) {
        $timeout(function() {
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

module.export = indigoTabScroller;

