(function() {
    angular
        .module('indigoeln')
        .directive('indigoHasAuthority', indigoHasAuthority);

    /* @ngInject */
    function indigoHasAuthority(Principal) {
        return {
            restrict: 'A',
            link: function($scope, $element, $attrs) {
                var authority = $attrs.indigoHasAuthority.replace(/\s+/g, '');

                if (authority.length > 0) {
                    defineVisibility(true);
                }

                function setVisible() {
                    $element.removeClass('hidden');
                }

                function defineVisibility(reset) {
                    if (reset) {
                        setVisible();
                    }

                    Principal.hasAuthority(authority).then(function(result) {
                        // TODO: should remove element if hasn't access
                        $element.toggleClass('hidden', !result);
                    });
                }
            }
        };
    }
})();
