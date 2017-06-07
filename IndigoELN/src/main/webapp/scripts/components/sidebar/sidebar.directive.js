(function () {
    angular
        .module('indigoeln')
        .directive('uuiToggleBox', function () {
            return {
                restrict: 'C',
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