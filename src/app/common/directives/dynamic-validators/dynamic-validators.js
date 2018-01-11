/* @ngInject */
function dynamicValidators($parse) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function($scope, elem, $attrs, ngModel) {
            var validators = $parse($attrs.dynamicValidators)($scope);
            _.forEach(validators, function(validatorFn, validationKey) {
                if (_.isFunction(validatorFn)) {
                    ngModel.$validators[validationKey] = validatorFn;
                }
            });
        }
    };
}

module.exports = dynamicValidators;
