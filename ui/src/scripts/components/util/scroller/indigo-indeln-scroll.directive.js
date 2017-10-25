(function() {
    angular
        .module('indigoeln')
        .directive('indigoIndelnScroll', indigoIndelnScroll);

    function indigoIndelnScroll($document) {
        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link(scope, iAttrs) {
            var scrollToTop = function() {
                var h = $(window).height();
                var elDocument = angular.element($document);

                elDocument.mousemove(function(e) {
                    var mousePosition = e.pageY - $(window).scrollTop();
                    var topRegion = 220;
                    var bottomRegion = h - 220;
                    if (isClickDown(e, mousePosition, topRegion, bottomRegion)) {
                        var distance = e.clientY - (h / 2);
                        // <- velocity
                        distance *= 0.1;
                        angular.element('#entities-content-id').scrollTop(distance + elDocument.scrollTop());
                    } else {
                        angular.element('#entities-content-id').unbind('mousemove');
                    }
                });

                function isClickDown(e, mousePosition, topRegion, bottomRegion) {
                    return e.which === 1 && (mousePosition < topRegion || mousePosition > bottomRegion);
                }
            };
            if (iAttrs.dragulaModel) {
                scope.$on(iAttrs.dragulaModel + '.drag', scrollToTop);
            }
        }
    }
})();
