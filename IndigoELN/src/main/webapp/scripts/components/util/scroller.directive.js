'use strict';
angular.module('indigoeln')
    .directive('myTabScroller', function ($timeout) {
        return {
            restrict: 'A',
            link: function (scope, iElement) {
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
                    scope.$on('$stateChangeSuccess', function () {
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
    .directive('myScroller', function () {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs) {
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