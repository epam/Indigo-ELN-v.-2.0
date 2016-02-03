'use strict';
angular.module('indigoeln')
    .config(function ($builderProvider) {
        $builderProvider.registerComponent('structureScheme', {
            templateUrl: "scripts/app/entities/template/components/structureScheme/structure-scheme.html",
            popoverTemplate: ""
        });
    });