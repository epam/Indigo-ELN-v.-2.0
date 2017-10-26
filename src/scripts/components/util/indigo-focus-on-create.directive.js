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
        function link(scope, element) {
            var $this = $(element),
                $cont = $this.parents('[scroller]').eq(0),
                top = $this.position().top + $this.outerHeight(true);

            $cont.animate({
                scrollTop: top
            }, 500);
        }
    }
})();
