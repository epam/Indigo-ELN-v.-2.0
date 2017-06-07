(function () {
    angular
        .module('indigoeln')
        .directive('indigoBatchStructure', indigoBatchStructure);

    function indigoBatchStructure() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/batchStructure/batchStructure.html'
        };
    }
})();