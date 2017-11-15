var template = require('./date-picker.html');

indigoDatePicker.$inject = ['formUtilsService'];

function indigoDatePicker(formUtilsService) {
    return {
        restrict: 'E',
        replace: true,
        require: '?^form',
        scope: {
            indigoLabel: '@',
            indigoLabelVertical: '=',
            indigoName: '@',
            indigoModel: '=',
            indigoReadonly: '=',
            indigoType: '@',
            indigoValidationRequired: '=',
            indigoClasses: '@'
        },
        compile: compile,
        template: template
    };

    /* @ngInject */
    function compile(tElement, tAttrs) {
        formUtilsService.doVertical(tAttrs, tElement);
        tElement.find('input').attr('timezone', moment.tz.guess());

        return {
            post: function(scope, iElement, iAttrs, formCtrl) {
                if (scope.indigoModel) {
                    scope.ctrl = {};
                    scope.ctrl.model = moment(scope.indigoModel);
                    scope.$watch('ctrl.model', function(date) {
                        scope.indigoModel = date ? date.toISOString() : null;
                    });
                }
                formUtilsService.showValidation(iElement, scope, formCtrl);
            }
        };
    }
}

module.exports = indigoDatePicker;

