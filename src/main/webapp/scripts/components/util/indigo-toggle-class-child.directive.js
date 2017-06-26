(function () {
    angular
        .module('indigoeln')
        .directive('indigoToggleClassChild', indigoToggleClassChild);

    function indigoToggleClassChild() {
        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link($scope, $element, $attrs) {
            var toggledClass = $attrs.indigoToggleClassChild;
            $element.bind('click', function (e) {
                e.stopPropagation();
                var child = $element.find('ul')[0];
                $(child).toggleClass(toggledClass);
            });
        }
    }
})();