var template = require('./structure-scheme.html');

function indigoStructureScheme() {
    return {
        restrict: 'E',
        template: template,
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

module.exports = indigoStructureScheme;
