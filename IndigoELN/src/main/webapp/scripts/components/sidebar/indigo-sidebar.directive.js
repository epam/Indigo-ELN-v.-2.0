(function () {
    angular
        .module('indigoeln')
        .directive('indigoSidebar', function () {
            return {
                restrict: 'A',
                link: link
            };
        });

    /* @ngInject */
    function link($scope, $element) {
        $element.on('click', function () {
            var $main = $('.main-container');
            $main.toggleClass('hide-menu');
            $main.toggleClass('open');
        });
    }
})();