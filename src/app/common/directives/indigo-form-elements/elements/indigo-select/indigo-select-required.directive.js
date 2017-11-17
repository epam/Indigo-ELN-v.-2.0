function indigoSelectRequired() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, elem, attrs, ngModel) {
            var isRequired = attrs.indigoSelectRequired === 'true';
            if (isRequired) {
                ngModel.$validators.indigoSelectRequired = function(modelValue) {
                    return !!(modelValue && modelValue.length > 0);
                };
            }
        }
    };
}

module.exports = indigoSelectRequired;
