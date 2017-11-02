(function() {
    angular
        .module('indigoeln')
        .directive('linkedExperiments', linkedExperiments);

    function linkedExperiments() {
        return {
            restrict: 'E',
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoReadonly: '=',
                indigoPlaceholder: '@',
                closeOnSelect: '='
            },
            controller: LinkedExperimentsController,
            controllerAs: 'vm',
            bindToController: true,
            compile: function($element, $attr) {
                if (!_.isUndefined($attr.indigoMultiple)) {
                    $element.find('ui-select').attr('multiple', '');
                }
            },
            templateUrl: 'scripts/components/form/elements/linked-experiments/linked-experiments.html'
        };

        function LinkedExperimentsController(componentHelper) {
            var vm = this;

            init();

            function init() {
                vm.refresh = refresh;
            }

            function refresh(query) {
                componentHelper.getExperiments().then(function(experiments) {
                    vm.items = _.filter(experiments, function(experiment) {
                        return experiment.name.startsWith(query);
                    });
                });
            }
        }
    }
})();
