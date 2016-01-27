/**
 * Created by Stepan_Litvinov on 1/25/2016.
 */
/* globals $ */
'use strict';
angular.module('indigoeln')
    .config(function ($builderProvider) {
        $builderProvider.registerComponent('name', {
            group: 'Default',
            label: 'Name',
            required: false,
            arrayToText: true,
            templateUrl: "scripts/app/entities/template/batches/productBatchDetails/productBatchDetails.html",
            popoverTemplate: ""
        });
    });