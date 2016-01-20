/* globals $ */
'use strict';

angular.module('indigoeln')
    .directive('showValidation', function () {
        return {
            restrict: 'A',
            require: 'form',
            link: function (scope, element) {
                element.find('.form-group').each(function () {
                    var $formGroup = $(this);
                    var $inputs = $formGroup.find('input[ng-model],textarea[ng-model],select[ng-model]');

                    if ($inputs.length > 0) {
                        $inputs.each(function () {
                            var $input = $(this);
                            scope.$watch(function () {
                                return $input.hasClass('ng-invalid') && $input.hasClass('ng-dirty');
                            }, function (isInvalid) {
                                $formGroup.toggleClass('has-error', isInvalid);
                            });
                        });
                    }
                });
            }
        };
    }).directive('myInput', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                inputLabel: '@',
                inputName: '@',
                inputModel: '=',
                inputReadonly: '=',
                inputType: '@',
                validationObj: '=',
                validationRequired: '=',
                validationMaxlength: '@',
                labelClasses: '@'
            },
            template: '<div class="form-group">' +
            '<label class="col-sm-2 control-label">{{inputLabel}}</label>' +
            '<div class="col-sm-10">' +
            '<input type="{{inputType}}" class="form-control" name="{{inputName}}" ng-model="inputModel" ng-readonly="inputReadonly" ng-required="validationRequired" ng-maxlength="validationMaxlength">' +
            '<div ng-show="validationObj.$invalid">' +
            '<p class="help-block" ng-show="validationObj.$error.required"> This field is required. </p>' +
            '<p class="help-block" ng-show="validationObj.$error.maxlength" > This field cannot be longer than {{validationMaxlength}} characters.</p>' +
            '</div>' +
            '</div>' +
            '</div>'
        };
    }).directive('myCheckbox', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                checkboxLabel: '@',
                checkboxModel: '=',
                checkboxName: '@'
            },
            template: '<div class="form-group">' +
            '<div class="col-sm-offset-2 col-sm-10">' +
            '<div class="checkbox">' +
            '<label>' +
            '<input type="checkbox" id="{{checkboxName}}" ng-model="checkboxModel"> {{checkboxLabel}}' +
            '</label> ' +
            '</div> ' +
            '</div> ' +
            '</div> '
        };
    }).directive('mySelect', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                selectLabel: '@',
                selectName: '@',
                selectModel: '=',
                selectItems: '='
            },
            template: '<div class="form-group">' +
            '<label class="col-sm-2 control-label">{{selectLabel}}</label>' +
            '<div class="col-sm-10">' +
            '<select class="form-control" multiple name="{{selectName}}" ng-model="selectModel" ng-options="item for item in selectItems" ></select>' +
            '</div>' +
            '</div>'
        };
    });
