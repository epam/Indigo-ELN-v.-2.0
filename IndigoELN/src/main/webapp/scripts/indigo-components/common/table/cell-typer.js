(function() {
    angular
        .module('indigoeln.Components')
        .directive('cellTyper', indigoTableVal);

    function indigoTableVal() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/table/cell-typer.html'
        };
    }
})();
