require('./simple-input.less');
var template = require('./simple-input.html');

/* @ngInject */
function simpleInput($compile, $log) {
    return {
        restrict: 'E',
        transclude: true,
        require: '?^form',
        scope: {
            validationPatternText: '@?'
        },
        link: function($scope, $element, $attr, formCtrl) {
            $element.addClass('form-group');
            var $input = $element.find('input');

            if ($input[0].attributes['ng-required'] || $input[0].attributes.required) {
                if (!formCtrl) {
                    $log.error('Elemenet hasn\'t form, but it required', $element);

                    return;
                }

                // TODO: check if ng-required evaluates to true. Otherwise this class is redundant
                $element.addClass('required');
                $scope.ngModelCtrl = formCtrl[$input.attr('name')];
                var el = $compile(template)($scope);
                $input.after(el);
            }
        },
        template: '<label ng-transclude></label>'
    };
}

module.exports = simpleInput;
