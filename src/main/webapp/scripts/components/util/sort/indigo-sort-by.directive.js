(function () {
    angular
        .module('indigoeln')
        .directive('indigoSortBy', indigoSortBy);

    function indigoSortBy() {
        return {
            restrict: 'A',
            scope: false,
            require: '^indigoSort',
            link: link
        };

        /* @ngInject */
        function link(scope, element, attrs, parentCtrl) {
            element.bind('click', function () {
                parentCtrl.sort(attrs.indigoSortBy);
                parentCtrl.applyClass(element);
            });
        }
    }
})();