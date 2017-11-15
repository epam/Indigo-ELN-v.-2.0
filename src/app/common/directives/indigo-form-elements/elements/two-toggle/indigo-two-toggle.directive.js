var template = require('./two-toggle.html');

indigoTwoToggle.$inject = ['formUtilsService'];

function indigoTwoToggle(formUtilsService) {
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
        template: template
    };

    /* @ngInject */
    function compile($element, tAttrs) {
        formUtilsService.doVertical(tAttrs, $element);
        if (tAttrs.indigoLabelVertical) {
            angular.element('<br/>').insertAfter($element.find('label').first());
        }

        return 'active';
    }
}

module.exports = indigoTwoToggle;
