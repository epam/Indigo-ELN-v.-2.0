var template = require('./text-area.html');

indigoTextArea.$inject = ['formUtilsService'];

function indigoTextArea(formUtilsService) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            indigoLabel: '@',
            indigoModel: '=',
            indigoNoElastic: '=',
            indigoInputGroup: '@',
            indigoReadonly: '=',
            indigoTooltip: '=',
            indigoTrim: '='
        },
        compile: compile,
        template: template
    };

    /* @ngInject */
    function compile(tElement, tAttrs) {
        formUtilsService.addDirectivesByAttrs(tAttrs, tElement.find('textarea'));

        if (tAttrs.indigoTrim) {
            tElement.find('textarea').attr('ng-trim', tAttrs.indigoTrim);
        }

        if (tAttrs.indigoNoElastic) {
            tElement.find('textarea').removeAttr('msd-elastic');
        }
    }
}

module.exports = indigoTextArea;
