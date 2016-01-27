/**
 * Created by Stepan_Litvinov on 1/25/2016.
 */
/* globals $ */
'use strict';
angular.module('indigoeln')
    .config(function ($builderProvider) {
        $builderProvider.registerComponent('productBatchDetails', {
            templateUrl: "scripts/app/entities/template/components/productBatchDetails/productBatchDetails.html",
            popoverTemplate: ""
        });
    });