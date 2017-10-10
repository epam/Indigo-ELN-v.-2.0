(function() {
    angular
        .module('indigoeln')
        .directive('simpleInput', indigoInput);

    /* @ngInject */
    function indigoInput() {
        return {
            restrict: 'E',
            scope: {
                label: '@',
                model: '=',
                isReadonly: '='
            },
            templateUrl: 'scripts/app/common/directives/simple-input/simple-input.html'
        };
    }
})();
