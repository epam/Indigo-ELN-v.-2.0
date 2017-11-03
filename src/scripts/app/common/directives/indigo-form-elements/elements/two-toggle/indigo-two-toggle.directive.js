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
            templateUrl: 'scripts/app/common/directives/indigo-form-elements/elements/two-toggle/two-toggle.html'
        };

        /* @ngInject */
        function compile($element, tAttrs) {
            formUtils.doVertical(tAttrs, $element);
            if (tAttrs.indigoLabelVertical) {
                angular.element('<br/>').insertAfter($element.find('label').first());
            }

            return 'active';
        }
    }
})();
