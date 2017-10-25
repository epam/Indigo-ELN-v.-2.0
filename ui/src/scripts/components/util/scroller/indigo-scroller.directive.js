(function() {
    angular
        .module('indigoeln')
        .directive('indigoScroller', indigoScroller);

    /* @ngInject */
    function indigoScroller(EntitiesBrowser, $timeout) {
        var scrollCache = {};

        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link(scope, $element, iAttrs) {
            var key;
            var prev = [];

            if (scope.indigoId && EntitiesBrowser.activeTab) {
                key = EntitiesBrowser.getActiveTab().$$title + scope.indigoId;
                var val = scrollCache[key];

                $timeout(function() {
                    $element.mCustomScrollbar('scrollTo', val || [0, 0], {
                        callbacks: false, scrollInertia: 0
                    });
                }, 500, false);
            }

            $element.addClass('my-scroller-axis-' + iAttrs.indigoScroller);
            $element.mCustomScrollbar({
                axis: iAttrs.indigoScroller,
                theme: iAttrs.indigoScrollerTheme || 'indigo',
                scrollInertia: 300,
                callbacks: {
                    onScroll: function() {
                        if (!key) {
                            return;
                        }
                        if (prev && prev[0] === this.mcs.top && prev[1] === this.mcs.left) {
                            return;
                        }
                        prev[0] = this.mcs.top;
                        prev[1] = this.mcs.left;
                        scrollCache[key] = [this.mcs.top, this.mcs.left];
                        $element.trigger('click');
                    }
                }
            });
        }
    }
})();
