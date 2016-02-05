/**
 * Created by Stepan_Litvinov on 1/25/2016.
 */
/* globals $ */
'use strict';
angular.module('indigoeln')
    .config(function ($builderProvider) {
        $builderProvider.registerComponent('conceptDetails', {
            templateUrl: "scripts/app/entities/template/components/conceptDetails/conceptDetails.html",
            popoverTemplate: ""
        });
    });