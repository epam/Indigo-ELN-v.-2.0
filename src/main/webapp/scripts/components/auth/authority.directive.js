(function () {
    angular
        .module('indigoeln')
        .directive('indigoHasAnyAuthority', indigoHasAnyAuthority)
        .directive('indigoHasAuthority', indigoHasAuthority);

    function indigoHasAnyAuthority(Principal) {
        return {
            restrict: 'A',
            link: angular.bind({Principal: Principal}, indigoHasAnyAuthorityLink)
        };
    }

    function indigoHasAuthority(Principal) {
        return {
            restrict: 'A',
            link: angular.bind({Principal: Principal}, indigoHasAuthorityLink)
        };
    }

    /* @ngInject */
    function indigoHasAnyAuthorityLink($scope, $element, $attrs) {
        var Principal = this.Principal;
        var authorities = $attrs.indigoHasAnyAuthority.replace(/\s+/g, '').split(',');

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

    /* @ngInject */
    function indigoHasAuthorityLink($scope, $element, $attrs) {
        var Principal = this.Principal;
        var authority = $attrs.indigoHasAuthority.replace(/\s+/g, '');
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