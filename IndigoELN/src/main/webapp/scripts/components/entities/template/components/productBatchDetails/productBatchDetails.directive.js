/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
/* globals $ */
'use strict';
angular.module('indigoeln')
    .directive('productBatchDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            controller: function($scope){
            },
            templateUrl: 'scripts/components/entities/template/components/productBatchDetails/productBatchDetails.html'
        };
    });