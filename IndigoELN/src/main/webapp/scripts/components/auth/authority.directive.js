'use strict';

angular.module('indigoeln')
    .directive('hasAnyAuthority', ['Principal', function (Principal) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var authorities = attrs.hasAnyAuthority.replace(/\s+/g, '').split(',');
                var setVisible = function () {
                    element.removeClass('hidden');
                };
                var setHidden = function () {
                    element.addClass('hidden');
                };
                var defineVisibility = function (reset) {
                    var result;
                    if (reset) {
                        setVisible();
                    }
                    result = Principal.hasAnyAuthority(authorities);
                    if (result) {
                        setVisible();
                    } else {
                        setHidden();
                    }
                };

                if (authorities.length > 0) {
                    defineVisibility(true);
                }
            }
        };
    }])
    .directive('hasAuthority', ['Principal', function (Principal) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var authority = attrs.hasAuthority.replace(/\s+/g, '');
                var setVisible = function () {
                    element.removeClass('hidden');
                };
                var setHidden = function () {
                    element.addClass('hidden');
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
        };
    }]);
