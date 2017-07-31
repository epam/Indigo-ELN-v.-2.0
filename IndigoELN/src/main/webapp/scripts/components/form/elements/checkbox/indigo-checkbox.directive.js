(function() {
    angular
        .module('indigoeln')
        .directive('indigoCheckbox', indigoCheckbox);

    /* @ngInject */
    function indigoCheckbox(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoName: '@',
                indigoClasses: '@',
                indigoDisabled: '=',
                indigoChange: '&',
                indigoClick: '&',
                indigoTooltip: '@',
                indigoTooltipPlacement: '@',
                isDirty: '='
            },
            compile: compile,
            templateUrl: 'scripts/components/form/elements/checkbox/checkbox.html'
        };

        /* @ngInject */
        function compile(tElement, tAttrs) {
            formUtils.clearLabel(tAttrs, tElement);
            var $checkbox = tElement.find('checkbox');
            formUtils.addDirectivesByAttrs(tAttrs, $checkbox);
            if (tAttrs.indigoModel) {
                $checkbox.removeAttr('ng-model-options');
            }

            return {
                post: function(scope) {
                    formUtils.addOnChange(scope);
                }
            };
        }
    }
})();
