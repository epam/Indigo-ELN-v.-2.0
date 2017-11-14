require('./simple-input.less');
var template = require('./simple-input.html');

simpleInput.$inject = ['$compile'];

function simpleInput($compile) {
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
