(function () {
    angular
        .module('indigoeln')
        .directive('indigoResizable', indigoResizable);

    function indigoResizable() {
        return {
            restrict: 'A',
            scope: {
                maxHeight: '=',
                minHeight: '='
            },
            link: link
        };

        /* @ngInject */
        function link($scope, $elem) {
            $elem.resizable({
                handles: 's',
                maxHeight: $scope.maxHeight,
                minHeight: $scope.minHeight
            });
        }
    }
})();