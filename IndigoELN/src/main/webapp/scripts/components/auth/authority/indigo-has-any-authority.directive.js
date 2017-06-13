(function () {
    angular
        .module('indigoeln')
        .directive('indigoHasAnyAuthority', indigoHasAnyAuthority);

    /* @ngInject */
    function indigoHasAnyAuthority(Principal) {
        return {
            restrict: 'A',
            link: angular.bind({Principal: Principal}, link)
        };
    }

    /* @ngInject */
    function link($scope, $element, $attrs) {
        var Principal = this.Principal,
            authorities = $attrs.indigoHasAnyAuthority.replace(/\s+/g, '').split(',');

        var setVisible = function () {
            $element.removeClass('hidden');
        };
        var setHidden = function () {
            $element.addClass('hidden');
        };
        var defineVisibility = function (reset) {
            if (reset) {
                setVisible();
            }

            Principal.hasAnyAuthority(authorities)
                .then(function (result) {
                    if (result) {
                        setVisible();
                    } else {
                        setHidden();
                    }
                });
        };

        if (authorities.length > 0) {
            defineVisibility(true);
        }
    }
})();