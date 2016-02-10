'use strict';
angular.module('indigoeln')
    .directive('structureScheme', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/structureScheme/structure-scheme.html',
            controller: 'StructureSchemeController'
        };
    });