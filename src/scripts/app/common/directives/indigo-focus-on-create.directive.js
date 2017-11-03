(function() {
    angular
        .module('indigoeln')
        .directive('indigoFocusOnCreate', indigoFocusOnCreate);

    function indigoFocusOnCreate() {
        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link(scope, $element) {
            var $cont = $element.parents('[scroller]').eq(0);
            var top = $element.position().top + $element.outerHeight(true);

            $cont.animate({
                scrollTop: top
            }, 500);
        }
    }
})();
