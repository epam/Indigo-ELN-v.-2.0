require('./indigo-scroller.less');
var PerfectScrollbar = require('perfect-scrollbar/dist/perfect-scrollbar');

/* @ngInject */
function indigoScroller($timeout) {
    return {
        restrict: 'A',
        link: link
    };

    function link($scope, $element, $attr) {
        $element.addClass('indigo-scroller indigo-scroller-axis-' + $attr.indigoScroller);

        var perfectScrollbar = new PerfectScrollbar($element[0], {
            useBothWheelAxes: true
        });

        // Update scrollbar to display immediately
        $timeout(function() {
            perfectScrollbar.update();
        }, 0);

        $scope.$on('$destroy', function() {
            perfectScrollbar.destroy();
            perfectScrollbar = null;
        });
    }
}

module.exports = indigoScroller;
