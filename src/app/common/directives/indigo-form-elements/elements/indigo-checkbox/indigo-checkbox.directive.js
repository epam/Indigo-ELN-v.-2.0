var template = require('./indigo-checkbox.html');

indigoCheckbox.$inject = ['formUtils'];

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
            indigoTooltipPlacement: '@'
        },
        compile: compile,
        template: template
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

module.exports = indigoCheckbox;
