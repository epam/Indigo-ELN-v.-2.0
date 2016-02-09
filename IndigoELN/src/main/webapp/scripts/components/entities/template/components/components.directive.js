/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';

angular.module('indigoeln')
    .constant('Components', [
        {name: 'Concept Details', id: "concept-details", desc: "Fake description"},
        {name: 'Product Batch Details', id: "product-batch-details", desc: "Fake description"}
    ])
    .directive('myComponent', function (Components) {
        return {
            restrict: 'A',
            replace: true,
            link: function (scope, iElement, iAttrs, controller) {
                scope.myComponent = iAttrs.myComponent;
            },
            template: '<div ng-switch="myComponent">' +
            '<div ng-switch-when="concept-details"><concept-details /></div>' +
            '<div ng-switch-when="product-batch-details"><product-batch-details /></div>' +
            '</div>'
        }
    });