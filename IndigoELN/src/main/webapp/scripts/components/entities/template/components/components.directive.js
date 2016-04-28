/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';

angular.module('indigoeln')
    .constant('Components', [
        {name: 'Concept Details', id: 'concept-details', desc: 'Fake description'},
        {name: 'Reaction Details', id: 'reaction-details', desc: 'Fake description'},
        {name: 'Product Batch Summary', id: 'product-batch-summary', desc: 'Fake description'},
        {name: 'Product Batch Details', id: 'product-batch-details', desc: 'Fake description'},
        {name: 'Reaction Scheme', id: 'reaction-scheme', desc: 'Fake description'},
        {name: 'Stoich Table', id: 'stoich-table', desc: 'Fake description'},
        {name: 'Batch Structure', id: 'batch-structure', desc: 'Fake description'},
        {name: 'Experiment Description', id: 'experiment-description', desc: 'Fake description'}
    ])
    .directive('myComponent', function () {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                myModel: '=',
                myExperiment: '=',
                myReadonly: '=',
                myExperimentForm: '=',
                myShare: '='
            },
            link: function (scope, iElement, iAttrs) {
                scope.myComponent = iAttrs.myComponent;
                scope.model = scope.myModel; //for capability
                scope.experimentForm = scope.myExperimentForm; //for capability
                scope.experiment = _.extend({}, scope.myExperiment); //for readonly
                scope.share = scope.myShare; //for communication between components
            },
            template: '<div ng-switch="myComponent">' +
            '<div ng-switch-when="concept-details"><concept-details /></div>' +
            '<div ng-switch-when="reaction-details"><reaction-details /></div>' +
            '<div ng-switch-when="product-batch-details"><product-batch-details /></div>' +
            '<div ng-switch-when="product-batch-summary"><product-batch-summary /></div>' +
            '<div ng-switch-when="reaction-scheme"><reaction-scheme /></div>' +
            '<div ng-switch-when="batch-structure"><batch-structure /></div>' +
            '<div ng-switch-when="stoich-table"><stoich-table /></div>' +
            '<div ng-switch-when="experiment-description"><experiment-description /></div>' +
            '</div>'
        };
    }).directive('myComponents', function () {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myTemplate: '=',
            myReadonly: '=',
            myModel: '=',
            myExperiment: '=',
            myExperimentForm: '='
        },
        link: function (scope) {
            scope.share = {}; //for communication between components
        },
        template: '<fieldset ng-disabled="myReadonly"><uib-tabset justified="true">' +
        '<uib-tab heading="{{tab.name}}" ng-repeat="tab in myTemplate track by tab.name">' +
        '<div ng-repeat="component in tab.components" my-component={{component.id}} my-model="myModel" my-experiment="myExperiment" my-experiment-form="myExperimentForm" my-share="share" my-readonly="myReadonly"></div>' +
        '</uib-tab>' +
        '</uib-tabset></fieldset>'
    };
});