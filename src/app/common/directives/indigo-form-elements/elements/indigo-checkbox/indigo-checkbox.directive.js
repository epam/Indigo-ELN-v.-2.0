var template = require('./indigo-checkbox.html');

indigoCheckbox.$inject = ['formUtilsService'];

function indigoCheckbox(formUtilsService) {
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
        formUtilsService.clearLabel(tAttrs, tElement);
        var $checkbox = tElement.find('checkbox');
        formUtilsService.addDirectivesByAttrs(tAttrs, $checkbox);
        if (tAttrs.indigoModel) {
            $checkbox.removeAttr('ng-model-options');
        }

        return {
            post: function(scope) {
                formUtilsService.addOnChange(scope);
            }
        };
    }
}

module.exports = indigoCheckbox;
