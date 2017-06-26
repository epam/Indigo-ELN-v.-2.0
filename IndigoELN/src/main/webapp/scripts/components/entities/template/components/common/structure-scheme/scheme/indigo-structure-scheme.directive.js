(function () {
    angular
        .module('indigoeln')
        .directive('indigoStructureScheme', indigoStructureScheme);

    function indigoStructureScheme() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/scheme/structure-scheme.html',
            controller: 'structureSchemeController',
            scope: {
                structureType: '=ssStructureType',
                title: '=ssTitle',
                autosave: '=ssautosave',
                model: '=ssModel',
                readonly: '=ssReadonly',
                share: '=ssShare',
                onChange: '&'
            },
            bindToController: true,
            controllerAs: 'vm'
        };
    }
})();