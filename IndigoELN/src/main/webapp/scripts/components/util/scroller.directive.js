'use strict';
angular.module('indigoeln')
    .directive('myScroller', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs, controller ) {
                $timeout(function () {
                    var $element = $(iElement);
                    $element.mCustomScrollbar({
                        axis: 'x',
                        theme: 'dark-thin',
                        callbacks: {
                            onUpdate: function () {
                                $element.mCustomScrollbar('scrollTo', '.active', {
                                    scrollInertia: 500
                                });
                            }
                        }
                    });
                    scope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                        $timeout(function () {
                            $element.mCustomScrollbar('update');
                            $element.mCustomScrollbar('scrollTo', '.active', {
                                scrollInertia: 500
                            });
                        });
                    });
                });
            }
        };
    });