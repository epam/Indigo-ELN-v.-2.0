(function () {
    angular
        .module('indigoeln')
        .directive('indigoIndelnScroll', indigoIndelnScroll);

    function indigoIndelnScroll() {
        return {
            restrict: 'A',
            link: link
        };
    }
    
    /* @ngInject */
    function link(scope, iAttrs) {
        var scrollToTop = function () {

            var h = $(window).height();
            $(document).mousemove(function (e) {
                var mousePosition = e.pageY - $(window).scrollTop();
                var topRegion = 220;
                var bottomRegion = h - 220;
                if (e.which == 1 && (mousePosition < topRegion || mousePosition > bottomRegion)) {    // e.wich = 1 => click down !
                    var distance = e.clientY - h / 2;
                    distance = distance * 0.1; // <- velocity
                    $('#entities-content-id').scrollTop(distance + $(document).scrollTop());
                } else {
                    $('#entities-content-id').unbind('mousemove');
                }
            });
        };
        if (iAttrs.dragulaModel) {
            scope.$on(iAttrs.dragulaModel + '.drag', function (el, source) {
                scrollToTop();
            });
        }
    }
})();