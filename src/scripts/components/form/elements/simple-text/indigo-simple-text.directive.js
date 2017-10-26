(function() {
    angular
        .module('indigoeln')
        .directive('indigoSimpleText', indigoSimpleText);

    function indigoSimpleText() {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoEmptyText: '@',
                indigoClasses: '@'
            },
            templateUrl: 'scripts/components/form/elements/simple-text/simple-text.html'
        };
    }
})();
