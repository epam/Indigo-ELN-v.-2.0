(function() {
    angular
        .module('indigoeln')
        .directive('indigoStructureScheme', indigoStructureScheme);

    function indigoStructureScheme() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/scheme/structure-scheme.html',
            controller: 'StructureSchemeController',
            scope: {
                structureType: '=ssStructureType',
                title: '=ssTitle',
                autosave: '=ssAutosave',
                model: '=ssModel',
                indigoReadonly: '=',
                share: '=ssShare',
                onChanged: '&'
            },
            bindToController: true,
            controllerAs: 'vm'
        };
    }
})();
