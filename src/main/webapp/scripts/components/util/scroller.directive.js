(function () {
    angular
        .module('indigoeln')
        .directive('indigoTabScroller', indigoTabScroller)
        .directive('indigoScroller', indigoScroller)
        .directive('indigoIndelnScroll', indigoIndelnScroll);

    /* @ngInject */
    function indigoTabScroller($timeout) {
        return {
            restrict: 'A',
            link: angular.bind({$timeout: $timeout}, indigoTabScrollerLink)
        };
    }

    /* @ngInject */
    function indigoScroller(EntitiesBrowser) {
        var scrollCache = {},
            bindings = {
                scrollCache: scrollCache,
                EntitiesBrowser: EntitiesBrowser
            };

        return {
            restrict: 'A',
            link: angular.bind(bindings, indigoScrollerLink)
        };
    }

    function indigoIndelnScroll() {
        return {
            restrict: 'A',
            link: indigoIndelnScrollLink
        };
    }

    /* @ngInject */
    function indigoTabScrollerLink(scope, iElement) {
        var $timeout = this.$timeout;

        $timeout(function () {
            var $element = $(iElement);
            $element.mCustomScrollbar({
                axis: 'x',
                theme: 'indigo',
                scrollInertia: 100
            });
            var onTabChanged = scope.$watch('activeTab', function () {
                $element.mCustomScrollbar('update');
                $timeout(function () {
                    var l = $element.find('.active').position().left;
                    $element.mCustomScrollbar('scrollTo', l, {
                        scrollInertia: 300
                    });
                }, 100);
            });
            scope.$on('$destroy', function () {
                onTabChanged()
            });
        }, 0, false);
    }

    /* @ngInject */
    function indigoScrollerLink(scope, iElement, iAttrs) {
        var scrollCache = this.scrollCache,
            EntitiesBrowser = this.EntitiesBrowser;

        if (scope.myId && EntitiesBrowser.activeTab) {
            var key = EntitiesBrowser.activeTab.$$title + scope.myId, val = scrollCache[key];
            setTimeout(function () {
                $element.mCustomScrollbar('scrollTo', val || [0, 0], {callbacks: false, scrollInertia: 0})
            }, 500);
        }

        var $element = $(iElement);
        $element.addClass('my-scroller-axis-' + iAttrs.indigoScroller);
        $element.mCustomScrollbar({
            axis: iAttrs.indigoScroller,
            theme: iAttrs.myScrollerTheme || 'indigo',
            scrollInertia: 300,
            callbacks: {
                onScroll: function (e) {
                    if (!key) return;
                    scrollCache[key] = [this.mcs.top, this.mcs.left];
                }
            }
        });
    }

    /* @ngInject */
    function indigoIndelnScrollLink(scope, iAttrs) {
        var scrollToTop = function () {

            var h = $(window).height();
            $(document).mousemove(function (e) {
                var mousePosition = e.pageY - $(window).scrollTop();
                var topRegion = 220;
                var bottomRegion = h - 220;
                if (e.which == 1 && (mousePosition < topRegion || mousePosition > bottomRegion)) {    // e.wich = 1 => click down !
                    var distance = e.clientY - h / 2;
                    distance = distance * 0.1; // <- velocity
                    $('#entities-content-id').scrollTop(distance + $(document).scrollTop());
                } else {
                    $('#entities-content-id').unbind('mousemove');
                }
            });
        };
        if (iAttrs.dragulaModel) {
            scope.$on(iAttrs.dragulaModel + '.drag', function (el, source) {
                scrollToTop();
            });
        }
    }
})();