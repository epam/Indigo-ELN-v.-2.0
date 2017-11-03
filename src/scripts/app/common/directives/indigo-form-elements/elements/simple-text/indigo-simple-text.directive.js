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
            templateUrl: 'scripts/app/common/directives/indigo-form-elements/elements/simple-text/simple-text.html'
        };
    }
})();
