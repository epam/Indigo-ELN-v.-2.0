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
                    var authorities = $attrs.indigoHasAuthority.replace(/\s+/g, '').split(',');

                    if (authorities.length > 0) {
                        defineVisibility(true);
                    }

                    function defineVisibility() {
                        Principal.hasAnyAuthority(authorities).then(function(isAvailable) {
                            if (!isAvailable) {
                                $element.remove();
                            }
                        });
                    }
                }
            }
        };
    }
})();
