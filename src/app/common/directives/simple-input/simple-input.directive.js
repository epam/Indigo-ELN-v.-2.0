simpleInput.$inject = ['$templateCache'];

function simpleInput($templateCache) {
    return {
        restrict: 'E',
        transclude: true,
        scope: {
            validationPatternText: '@?'
        },
        compile: function($element) {
            $element.addClass('form-group');
            var $input = $element.find('input');

            if ($input.attr('ng-required')) {
                $input.after($templateCache.get('inputModelInvalid'));
            }
        },
        template: '<label ng-transclude></label>'
    };
}

module.exports = simpleInput;
