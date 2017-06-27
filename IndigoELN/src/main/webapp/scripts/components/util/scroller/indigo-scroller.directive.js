(function() {
    angular
        .module('indigoeln')
        .directive('indigoScroller', indigoScroller);

    /* @ngInject */
    function indigoScroller(EntitiesBrowser) {
        var scrollCache = {};

        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link(scope, iElement, iAttrs) {
            if (scope.indigoId && EntitiesBrowser.activeTab) {
                var key = EntitiesBrowser.getActiveTab().$$title + scope.indigoId,
                    val = scrollCache[key];
                setTimeout(function() {
                    $element.mCustomScrollbar('scrollTo', val || [0, 0], {
                        callbacks: false, scrollInertia: 0
                    });
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
                    onScroll: function(e) {
                        if (!key) {
                            return;
                        }
                        if (prev && prev[0] == this.mcs.top && prev[1] == this.mcs.left) {
                            return;
                        }
                        prev[0] = this.mcs.top;
                        prev[1] = this.mcs.left;
                        scrollCache[key] = [this.mcs.top, this.mcs.left];
                        iElement.trigger('click'); // // will close some popups EPMLSOPELN-437
                    }
                }
            });
        }
    }
})();
