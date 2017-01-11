/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
angular.module('indigoeln')
    .constant('Components', [
        {
            name: 'Concept Details',
            id: 'concept-details',
            desc: 'Allows user to specify Title, Therapeutic Area, Project Code, Co-authors, Designers, Keywords and Linked Experiments'
        },
        {
            name: 'Reaction Details',
            id: 'reaction-details',
            desc: 'Allows user to specify Title, Therapeutic Area, Project Code, Co-authors, Literature References, Keywords, Link to Previous and Future Experiments and Link to any Experiment'
        },
        {
            name: 'Product Batch Summary',
            id: 'product-batch-summary',
            desc: 'Represents all batches in table format. Allows user to review, create and edit batch details:Total amount weight, Yield%, Stereoisomer code, Purity, Solubility, Hazards,ect. Batch Registration (if it is allowed) is also executed here'
        },
        {
            name: 'Product Batch Details',
            id: 'product-batch-details',
            desc: 'Provides details for the individual batch. Allows user to review, create and edit batch details:Total amount weight, Yield%, Stereoisomer code, Purity, Solubility, Hazards,ect. Batch Registration (if it is allowed) is also executed here'
        },
        {
            name: 'Reaction Scheme',
            id: 'reaction-scheme',
            desc: 'Allows user to draw , import and export reaction schema'
        },
        {
            name: 'Stoich Table',
            id: 'stoich-table',
            desc: 'Allows user to specify Reactants, Reagents and Solvent using automatic reaction scheme analysis or manual search in database(s). Stoichiometry calculations of the Initial amounts and Theoretical amounts f the Intended Reaction Products are executed here'
        },
        {
            name: 'Batch Structure',
            id: 'batch-structure',
            desc: 'Allows user to draw , import and export batch structure'
        },
        {
            name: 'Experiment Description',
            id: 'experiment-description',
            desc: 'Contains text editor with possibility to text formatting, insert pictures and table'
        },
        {
            name: 'Attachments',
            id: 'attachments',
            desc: 'Allows to manage attachment of any kind of file related to  this experiment'
        },
        {
            name: 'Preferred Compounds Summary',
            id: 'preferred-compounds-summary',
            desc: 'Allows user to review, create and edit compounds details: Stereoisomer code, Comments, ect. Virtual Compound Registration (if it is allowed) is also executed here.'
        },
        {
            name: 'Preferred Compound  Details',
            id: 'preferred-compound-details',
            desc: 'Provides details for the individual compound. Allows user to review, create and edit batch details: Stereoisomer code, Comments, ect. Virtual Compound Registration (if it is allowed) is also executed here.'
        }
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
                //for capability
                scope.model = scope.myModel;
                //for capability
                scope.experimentForm = scope.myExperimentForm;
                //for readonly
                scope.experiment = _.extend({}, scope.myExperiment);
                //for communication between components
                scope.share = scope.myShare;
            },
            template: '<div ng-switch="myComponent">' +
            '<div ng-switch-when="concept-details"><concept-details /></div>' +
            '<div ng-switch-when="reaction-details"><reaction-details /></div>' +
            '<div ng-switch-when="product-batch-details"><product-batch-details /></div>' +
            '<div ng-switch-when="product-batch-summary"><product-batch-summary /></div>' +
            '<div ng-switch-when="preferred-compounds-summary"><preferred-compounds-summary /></div>' +
            '<div ng-switch-when="preferred-compound-details"><preferred-compound-details /></div>' +
            '<div ng-switch-when="reaction-scheme"><reaction-scheme /></div>' +
            '<div ng-switch-when="batch-structure"><batch-structure /></div>' +
            '<div ng-switch-when="stoich-table"><stoich-table /></div>' +
            '<div ng-switch-when="experiment-description"><experiment-description /></div>' +
            '<div ng-switch-when="attachments"><attachments /></div>' +
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
            //for communication between components
            scope.share = {};
        },
        template: '<fieldset ng-disabled="myReadonly"><uib-tabset class="inner-tabs">' +
        '<uib-tab class="" heading="{{tab.name}}" ng-repeat="tab in myTemplate track by tab.name">' +
        '<div class="my-component" ng-repeat="component in tab.components" my-component={{component.id}} my-model="myModel" my-experiment="myExperiment" my-experiment-form="myExperimentForm" my-share="share" my-readonly="myReadonly"></div>' +
        '</uib-tab>' +
        '</uib-tabset></fieldset>'
    };
});