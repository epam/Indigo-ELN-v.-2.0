(function() {
    angular
        .module('indigoeln')
        .run(run)
        .directive('simpleInput', simpleInput);

    simpleInput.$inject = ['$templateCache'];

    function simpleInput($templateCache) {
        return {
            restrict: 'E',
            transclude: true,
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

    run.$inject = ['$templateCache'];

    function run($templateCache) {
        $templateCache.put('inputModelInvalid', '<div ng-if="ngModelCtrl.$invalid">' +
            '<p class="help-block" ng-if="ngModelCtrl.$error.required">This field is required. </p>' +
            '<p class="help-block" ng-if="ngModelCtrl.$error.maxlength">This field can\t be longer than' +
            '<span ng-bind="ngModelCtrl.maxlength"></span> characters.</p>' +
            '<p class="help-block" ng-if="ngModelCtrl.$error.pattern"' +
            ' ng-bind="indigoValidationPatternText"></p></div>');
    }
})();
