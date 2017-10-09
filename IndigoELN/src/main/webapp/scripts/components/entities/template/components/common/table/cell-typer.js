(function() {
    angular
        .module('indigoeln')
        .directive('cellTyper', indigoTableVal);

    function indigoTableVal() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/table/cell-typer.html'
        };
    }
})();
