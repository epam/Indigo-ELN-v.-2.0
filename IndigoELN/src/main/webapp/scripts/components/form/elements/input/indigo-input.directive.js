(function () {
    angular
        .module('indigoeln')
        .directive('indigoInput', indigoInput);

    /* @ngInject */
    function indigoInput(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            require: '?^form',
            scope: {
                indigoLabel: '@',
                indigoLabelVertical: '=',
                indigoLabelColumnsNum: '=',
                indigoName: '@',
                indigoModel: '=',
                indigoReadonly: '=',
                indigoType: '@',
                indigoInputGroup: '@',
                indigoInputSize: '@',
                indigoChange: '&',
                indigoClick: '&',
                indigoValidationRequired: '=',
                indigoValidationMaxlength: '@',
                indigoValidationMinlength: '@',
                indigoValidationPattern: '@',
                indigoValidationPatternText: '@',
                indigoClasses: '@',
                indigoParsers: '=',
                indigoFormatters: '=',
                indigoTooltip: '='
            },
            compile: angular.bind({formUtils: formUtils}, compile),
            templateUrl: 'scripts/components/form/elements/input/input.html'
        };
    }

    /* @ngInject */
    function compile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        formUtils.doVertical(tAttrs, tElement);
        var $input = tElement.find('input');
        if (tAttrs.indigoInputGroup) {
            var elementIg = $('<div class="input-group"/>');
            if (tAttrs.indigoInputSize) {
                elementIg.addClass('input-group-' + tAttrs.indigoInputSize);
            }
            var inputGroup = $input.wrap(elementIg).parent();
            if (tAttrs.indigoInputGroup === 'append') {
                inputGroup.append('<div class="input-group-btn" ng-transclude/>');
            } else if (tAttrs.indigoInputGroup === 'prepend') {
                inputGroup.prepend('<div class="input-group-btn" ng-transclude/>');
            }
        }
        formUtils.clearLabel(tAttrs, tElement);
        formUtils.setLabelColumns(tAttrs, tElement);
        formUtils.addDirectivesByAttrs(tAttrs, $input);
        $input.attr('title', '{{indigoModel}}');
        return {
            post: function (scope, iElement, iAttrs, formCtrl) {
                formUtils.showValidation(iElement, scope, formCtrl);
                formUtils.addOnChange(scope);
            }
        };
    }
})();