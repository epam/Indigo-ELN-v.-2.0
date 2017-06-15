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

        if (scope.indigoId && EntitiesBrowser.activeTab) {
            var key = EntitiesBrowser.activeTab.$$title + scope.indigoId, val = scrollCache[key];
            setTimeout(function () {
                $element.mCustomScrollbar('scrollTo', val || [0, 0], {callbacks: false, scrollInertia: 0});
            }, 500);
        }

        var $element = $(iElement);
        $element.addClass('my-scroller-axis-' + iAttrs.indigoScroller);
        var prev = [];
        $element.mCustomScrollbar({
            axis: iAttrs.indigoScroller,
            theme: iAttrs.indigoScrollerTheme || 'indigo',
            scrollInertia: 300,
            callbacks: {
                onScroll: function (e) {
                    if (!key) return;
                    if (prev && prev[0] ==  this.mcs.top && prev[1] ==  this.mcs.left) return;
                    prev[0] =  this.mcs.top;
                    prev[1] =  this.mcs.left;
                    scrollCache[key] = [this.mcs.top, this.mcs.left];
                    iElement.trigger('click'); //// will close some popups EPMLSOPELN-437
                }
            }
        });
    }
})();