'use strict';
angular.module('indigoeln')
    .directive('myTabScroller', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs, controller) {
                $timeout(function () {
                    var $element = $(iElement);
                    $element.mCustomScrollbar({
                        axis: 'x',
                        theme: 'indigo',
                        scrollInertia: 100,
                        callbacks: {
                            onUpdate: function () {
                                $element.mCustomScrollbar('scrollTo', '.active', {
                                    scrollInertia: 100
                                });
                            }
                        }
                    });
                    scope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
                        $timeout(function () {
                            $element.mCustomScrollbar('update');
                            $element.mCustomScrollbar('scrollTo', '.active', {
                                scrollInertia: 100
                            });
                        });
                    });
                });
            }
        };
    })
    .directive('myScroller', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs, controller) {
                var $element = $(iElement);
                $element.addClass('my-scroller-axis-' + iAttrs.myScroller);
                $element.mCustomScrollbar({
                    axis: iAttrs.myScroller,
                    theme: iAttrs.myScrollerTheme || "indigo",
                    scrollInertia: 300
                });
            }
        };
    });