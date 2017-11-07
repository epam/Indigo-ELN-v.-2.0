indigoHasAuthority.$inject = ['principalService'];

function indigoHasAuthority(principalService) {
    return {
        restrict: 'A',
        link: {
            pre: function($scope, $element, $attrs) {
                var authorities = $attrs.indigoHasAuthority.replace(/\s+/g, '');

                if (authorities.length > 0) {
                    defineVisibility(true);
                }

                function defineVisibility() {
                    if (!principalService.hasAuthority(authorities)) {
                        $element.remove();
                    }
                }
            }
        }
    };
}

module.exports = indigoHasAuthority;
