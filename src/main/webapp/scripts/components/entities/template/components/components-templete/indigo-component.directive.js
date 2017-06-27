(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

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
            link: link,
            templateUrl: 'scripts/components/entities/template/components/components-templete/component.html'
        };

        /* @ngInject */
        function link(scope, iElement, iAttrs) {
            scope.myComponent = iAttrs.indigoComponent;
            // for capability
            scope.model = scope.indigoModel;
            // for capability
            scope.experimentForm = scope.indigoExperimentForm;
            // for readonly
            scope.experiment = _.extend({}, scope.indigoExperiment);
            // for communication between components
            scope.share = scope.indigoShare;
        }
    }
})();
