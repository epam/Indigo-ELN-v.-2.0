'use strict';

angular.module('indigoeln')
    .directive('activeLink', function(location) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var clazz = attrs.activeLink;
                var path = attrs.href;
                path = path.substring(1); //hack because path does bot return including hashbang
                scope.location = location;
                scope.$watch('location.path()', function(newPath) {
                    if (path === newPath) {
                        element.addClass(clazz);
                    } else {
                        element.removeClass(clazz);
                    }
                });
            }
        };
    }).directive('innerMenuToggleButton', function () {
    return {
        restrict: 'C',
        link: function (scope, element) {
            element.on('click', function () {
                var $main = $('.main-container');
                $main.toggleClass('hide-menu');
                $main.toggleClass('open');
            });
        }
    };
    });
