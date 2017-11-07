run.$inject = ['$templateCache'];

function run($templateCache) {
    $templateCache.put('inputModelInvalid', '<div ng-if="ngModelCtrl.$invalid">' +
        '<p class="help-block" ng-if="ngModelCtrl.$error.required">This field is required. </p>' +
        '<p class="help-block" ng-if="ngModelCtrl.$error.maxlength">This field can\t be longer than' +
        '<span ng-bind="ngModelCtrl.maxlength"></span> characters.</p>' +
        '<p class="help-block" ng-if="ngModelCtrl.$error.pattern"' +
        ' ng-bind="validationPatternText"></p></div>');
}

module.export = run;
