(function() {
    angular
        .module('indigoeln')
        .directive('indigoHasAuthority', indigoHasAuthority);

    /* @ngInject */
    function indigoHasAuthority(Principal) {
        return {
            restrict: 'A',
            link: {
                pre: function($scope, $element, $attrs) {
                    var authorities = $attrs.indigoHasAuthority.replace(/\s+/g, '');

                    if (authorities.length > 0) {
                        defineVisibility(true);
                    }

                    function defineVisibility() {
                        if (!Principal.hasAuthority(authorities)) {
                            $element.remove();
                        }
                    }
                }
            }
        };
    }
})();
