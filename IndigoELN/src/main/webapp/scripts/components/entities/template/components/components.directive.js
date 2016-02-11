/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';

angular.module('indigoeln')
    .constant('Components', [
        {name: 'Concept Details', id: "concept-details", tab: 'Concept', desc: "Fake description"},
        {name: 'Reaction Details', id: "reaction-details", tab: 'Experiments', desc: "Fake description"},
        {name: 'Product Batch Details', id: "product-batch-details", tab: 'Batches', desc: "Fake description"}
    ])
    .directive('myComponent', function () {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                myModel: '='
            },
            link: function (scope, iElement, iAttrs, controller) {
                scope.myComponent = iAttrs.myComponent;
                scope.model = scope.myModel; //for capability
            },
            template: '<div ng-switch="myComponent">' +
            '<div ng-switch-when="concept-details"><concept-details /></div>' +
            '<div ng-switch-when="reaction-details"><reaction-details /></div>' +
            '<div ng-switch-when="product-batch-details"><product-batch-details /></div>' +
            '<div ng-switch-when="product-batch-summary"><product-batch-summary /></div>' +
            '</div>'
        }
    }).directive('myComponents', function () {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myTemplate: '=',
            myDisabled: '=',
            myModel: '='
        },
        template: '<fieldset ng-disabled="myDisabled"><uib-tabset justified="true">' +
        '<uib-tab heading="{{key}}" ng-repeat="(key,value) in myTemplate | groupBy: \'tab\'">' +
        '<div ng-repeat="component in value" my-component={{component.id}} my-model="myModel"></div>' +
        '</uib-tab>' +
        '</uib-tabset></fieldset>'
    }
});