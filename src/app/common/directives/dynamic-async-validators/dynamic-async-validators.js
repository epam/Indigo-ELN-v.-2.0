/* @ngInject */
function dynamicAsyncValidators($parse) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function($scope, elem, $attrs, ngModel) {
            var validators = $parse($attrs.dynamicAsyncValidators)($scope);
            _.forEach(validators, function(validatorFn, validationKey) {
                if (_.isFunction(validatorFn)) {
                    ngModel.$asyncValidators[validationKey] = validatorFn;
                }
            });
        }
    };
}

module.exports = dynamicAsyncValidators;
