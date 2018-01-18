var PerfectScrollbar = require('perfect-scrollbar/dist/perfect-scrollbar');

indigoTabScroller.$inject = ['$timeout'];

function indigoTabScroller($timeout) {
    return {
        restrict: 'A',
        link: link
    };

    /* @ngInject */
    function link($scope, $element) {
        $element.addClass('indigo-scroller');

        var perfectScrollbar = new PerfectScrollbar($element[0], {
            suppressScrollY: true,
            useBothWheelAxes: true
        });

        // Update scrollbar to display immediately
        $timeout(function() {
            perfectScrollbar.update();
        }, 0);

        // Update scrollbar on container resize
        $scope.$watch(function() {
            return $scope.vm.tabs && _.keys($scope.vm.tabs).length;
        }, function() {
            perfectScrollbar.update();
        });

        $scope.$watch('vm.activeTab', function() {
            $timeout(function() {
                var targetElement = $element.find('.active');

                targetElement[0].scrollIntoView();
            }, 100);
        });

        $scope.$on('$destroy', function() {
            perfectScrollbar.destroy();
            perfectScrollbar = null;
        });
    }
}

module.exports = indigoTabScroller;

