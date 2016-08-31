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
                        }, 0, false);
                    });
                }, 0, false);
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
                    theme: iAttrs.myScrollerTheme || 'indigo',
                    scrollInertia: 300
                });
            }
        };
    }).directive('indelnScroll', function () {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs) {
                var scrollToTop = function () {
                    var h = $(window).height();
                    $(document).mousemove(function(e) {
                        var mousePosition = e.pageY - $(window).scrollTop();
                        var topRegion = 220;
                        var bottomRegion = h - 220;
                        if(e.which == 1 && (mousePosition < topRegion || mousePosition > bottomRegion)){    // e.wich = 1 => click down !
                            var distance = e.clientY - h / 2;
                            distance = distance * 0.1; // <- velocity
                            $('#entities-content-id').scrollTop( distance + $(document).scrollTop()) ;
                        }else{
                            $('#entities-content-id').unbind('mousemove');
                        }
                    });
                };
               if(iAttrs.dragulaModel){
                   scope.$on(iAttrs.dragulaModel + '.drag', function(el, source) {
                       scrollToTop();
                   });
               }
            }
        };
    });