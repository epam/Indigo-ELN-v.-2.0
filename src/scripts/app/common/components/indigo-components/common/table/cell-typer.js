(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('cellTyper', indigoTableVal);

    function indigoTableVal() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/common/components/indigo-components/common/table/cell-typer.html'
        };
    }
})();
