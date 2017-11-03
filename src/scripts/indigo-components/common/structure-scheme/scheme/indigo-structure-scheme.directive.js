(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoStructureScheme', indigoStructureScheme);

    function indigoStructureScheme() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/indigo-components/common/structure-scheme/scheme/structure-scheme.html',
            controller: 'StructureSchemeController',
            scope: {
                structureType: '@ssStructureType',
                title: '=ssTitle',
                autosave: '=ssAutosave',
                model: '=ssModel',
                modelTrigger: '=',
                isReadonly: '=',
                onChanged: '&'
            },
            bindToController: true,
            controllerAs: 'vm'
        };
    }
})();
