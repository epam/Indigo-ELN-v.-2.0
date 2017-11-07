var template = require('./linked-experiments.html');

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
        template: template
    };

    function LinkedExperimentsController(commonHelper) {
        var vm = this;

        init();

        function init() {
            vm.refresh = refresh;
        }

        function refresh(query) {
            commonHelper.getExperiments().then(function(experiments) {
                vm.items = _.filter(experiments, function(experiment) {
                    return experiment.name.startsWith(query);
                });
            });
        }
    }
}

module.export = linkedExperiments;
