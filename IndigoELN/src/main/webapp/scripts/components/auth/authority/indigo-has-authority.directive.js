(function () {
    angular
        .module('indigoeln')
        .directive('indigoHasAuthority', indigoHasAuthority);

    /* @ngInject */
    function indigoHasAuthority(Principal) {
        return {
            restrict: 'A',
            link: angular.bind({Principal: Principal}, link)
        };
    }

    /* @ngInject */
    function link($scope, $element, $attrs) {
        var Principal = this.Principal,
            authority = $attrs.indigoHasAuthority.replace(/\s+/g, '');

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
            Principal.hasAuthority(authority)
                .then(function (result) {
                    if (result) {
                        setVisible();
                    } else {
                        setHidden();
                    }
                });
        };

        if (authority.length > 0) {
            defineVisibility(true);
        }
    }
})();