(function () {
    angular
        .module('indigoeln')
        .directive('indigoTabContent', indigoTabContent);

    function indigoTabContent() {
        return {
            restrict: 'A',
            link: link
        };
    }

    /* @ngInject */
    function link($scope, $timeout, $window, iElement) {
        $timeout(function () {
            var $element = $(iElement);
            var $panelBody = $element.find('.panel-body:first');
            $panelBody.css('overflow-y', 'auto');
            var $footer = $panelBody.next();

            var $$window = $($window);
            var updateHeight = _.debounce(function () {
                var height = $$window.height() - $panelBody.offset().top - $footer.outerHeight();
                $panelBody.css('height', height + 'px');
            }, 300);

            angular.element($window).bind('resize', function () {
                updateHeight();
            });
            updateHeight();
        }, 0, false);
    }
})();