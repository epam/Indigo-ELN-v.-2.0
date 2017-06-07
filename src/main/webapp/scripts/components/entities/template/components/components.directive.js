(function () {
    angular
        .module('indigoeln')
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
        .directive('indigoComponent', indigoComponent)
        .directive('indigoComponents', indigoComponents);

    /* @ngInject */
    function indigoComponents($timeout) {
        var scrollCache = {}

        var bindings = {
            scrollCache: scrollCache,
            $timeout: $timeout
        };

        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoTemplate: '=',
                indigoReadonly: '=',
                indigoModel: '=',
                indigoExperiment: '=',
                indigoExperimentForm: '='
            },
            link: angular.bind(bindings, indigoComponentsLink),
            template: '<fieldset ng-disabled="indigoReadonly" ><uib-tabset class="inner-tabs">' +
            '<uib-tab class="" heading="{{tab.name}}" ng-repeat="tab in indigoTemplate track by tab.name">' +
            '<div class="my-component"  ng-repeat="component in tab.components" indigo-component={{component.id}} indigo-model="indigoModel" indigo-experiment="indigoExperiment" indigo-experiment-form="indigoExperimentForm" indigo-share="share" indigo-readonly="indigoReadonly"></div>' +
            '</uib-tab>' +
            '</uib-tabset></fieldset>'
        };
    }

    /* @ngInject */
    function indigoComponentsLink(scope, element) {
        var scrollCache = this.scrollCache,
            $timeout = this.$timeout;

        if (!scope.indigoExperiment) return;
        var id = scope.indigoExperiment.fullId, tc, preventFirstScroll;
        $timeout(function () {
            tc = element.find('.tab-content');
            if (scrollCache[id]) {
                setTimeout(function () {
                    nostore = true;
                    tc[0].scrollTop = scrollCache[id];
                }, 100);
            }
            var stimeout, nostore;
            tc.on('scroll', function (e) {
                if (nostore) {
                    nostore = false;
                    return;
                }
                if (!preventFirstScroll) {
                    scrollCache[id] = this.scrollTop;
                } else {
                    nostore = true;
                    tc[0].scrollTop = scrollCache[id] || 0;
                }
                clearTimeout(stimeout)
                stimeout = setTimeout(function () {
                    preventFirstScroll = true;
                }, 300);
                preventFirstScroll = false;
                nostore = false;
            });
        }, 100);

        //for communication between components
        scope.share = {};
    }

    function indigoComponent() {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                indigoModel: '=',
                indigoExperiment: '=',
                indigoReadonly: '=',
                indigoExperimentForm: '=',
                indigoShare: '='
            },
            link: indigoComponentLink,
            template: '<div ng-switch="myComponent">' +
            '<div ng-switch-when="concept-details"><indigo-concept-details /></div>' +
            '<div ng-switch-when="reaction-details"><indigo-reaction-details /></div>' +
            '<div ng-switch-when="product-batch-details"><indigo-product-batch-details /></div>' +
            '<div ng-switch-when="product-batch-summary"><indigo-product-batch-summary /></div>' +
            '<div ng-switch-when="preferred-compounds-summary"><indigo-preferred-compounds-summary /></div>' +
            '<div ng-switch-when="preferred-compound-details"><indigo-preferred-compound-details /></div>' +
            '<div ng-switch-when="reaction-scheme"><indigo-reaction-scheme /></div>' +
            '<div ng-switch-when="batch-structure"><indigo-batch-structure /></div>' +
            '<div ng-switch-when="stoich-table"><indigo-stoich-table /></div>' +
            '<div ng-switch-when="experiment-description"><indigo-experiment-description /></div>' +
            '<div ng-switch-when="attachments"><indigo-attachments /></div>' +
            '</div>'
        };
    }

    /* @ngInject */
    function indigoComponentLink(scope, iElement, iAttrs) {
        scope.myComponent = iAttrs.indigoComponent;
        //for capability
        scope.model = scope.indigoModel;
        //for capability
        scope.experimentForm = scope.indigoExperimentForm;
        //for readonly
        scope.experiment = _.extend({}, scope.indigoExperiment);
        //for communication between components
        scope.share = scope.indigoShare;
    }

})();