(function () {
    angular
        .module('indigoeln')
        .directive('indigoScroller', indigoScroller);

    /* @ngInject */
    function indigoScroller(EntitiesBrowser) {
        var scrollCache = {},
            bindings = {
                scrollCache: scrollCache,
                EntitiesBrowser: EntitiesBrowser
            };

        return {
            restrict: 'A',
            link: angular.bind(bindings, link)
        };
    }

    /* @ngInject */
    function link(scope, iElement, iAttrs) {
        var scrollCache = this.scrollCache,
            EntitiesBrowser = this.EntitiesBrowser;

        if (scope.myId && EntitiesBrowser.activeTab) {
            var key = EntitiesBrowser.activeTab.$$title + scope.myId, val = scrollCache[key];
            setTimeout(function () {
                $element.mCustomScrollbar('scrollTo', val || [0, 0], {callbacks: false, scrollInertia: 0});
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
})();