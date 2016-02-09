/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';

angular.module('indigoeln')
    .constant('Components', [
        'concept-details',
        'product-batch-details'
    ])
    .directive('myComponent', function (Components) {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                myComponent: '@'
            },
            template: '<div ng-switch="myComponent">' +
            '<div ng-switch-when="concept-details"><concept-details /></div>' +
            '<div ng-switch-when="product-batch-details"><product-batch-details /></div>' +
            '</div>'
        }
    });