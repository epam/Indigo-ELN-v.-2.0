(function() {
    angular
        .module('indigoeln')
        .directive('indigoHasAnyAuthority', indigoHasAnyAuthority);

    /* @ngInject */
    function indigoHasAnyAuthority(principalService) {
        return {
            restrict: 'A',
            link: function($scope, $element, $attrs) {
                var authorities = $attrs.indigoHasAnyAuthority.replace(/\s+/g, '').split(',');

                if (authorities.length > 0) {
                    defineVisibility(true);
                }

                function setVisible() {
                    $element.removeClass('hidden');
                }

                function defineVisibility(reset) {
                    if (reset) {
                        setVisible();
                    }

                    $element.toggleClass('hidden', !principalService.hasAnyAuthority(authorities));
                }
            }
        };
    }
})();
