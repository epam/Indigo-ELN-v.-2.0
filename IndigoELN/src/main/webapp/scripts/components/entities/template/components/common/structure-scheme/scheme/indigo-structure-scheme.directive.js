(function () {
    angular
        .module('indigoeln')
        .directive('indigoStructureScheme', indigoStructureScheme);

    function indigoStructureScheme() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/scheme/structure-scheme.html',
            controller: 'StructureSchemeController'
        };
    }
})();