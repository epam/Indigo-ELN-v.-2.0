(function() {
    angular
        .module('indigoeln')
        .directive('indigoTwoToggle', indigoTwoToggle);

    /* @ngInject */
    function indigoTwoToggle(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoFirst: '@',
                indigoSecond: '@',
                indigoLabelVertical: '=',
                indigoClasses: '@',
                indigoReadonly: '='
            },
            compile: compile,
            templateUrl: 'scripts/components/form/elements/two-toggle/two-toggle.html'
        };

        /* @ngInject */
        function compile(tElement, tAttrs) {
            formUtils.doVertical(tAttrs, tElement);
            if (tAttrs.indigoLabelVertical) {
                $('<br/>').insertAfter(tElement.find('label').first());
            }

            var active = 'active';

            return active;
        }
    }
})();
