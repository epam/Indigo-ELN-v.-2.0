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
                validationObj: '=',
                validationRequired: '=',
                validationMaxlength: '@'
            },
            template: '<div class="form-group">' +
            '<label>{{inputLabel}}</label>' +
            '<input type="text" class="form-control" name="{{inputName}}" ng-model="inputModel" ng-readonly="inputReadonly" ng-required="validationRequired" ng-maxlength="validationMaxlength">' +
            '<div ng-show="validationObj.$invalid">' +
            '<p class="help-block" ng-show="validationObj.$error.required"> This field is required. </p>' +
            '<p class="help-block" ng-show="validationObj.$error.maxlength" > This field cannot be longer than {{validationMaxlength}} characters.</p>' +
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
            '<label for="{{checkboxName}}">' +
            '<input type="checkbox" id="{{checkboxName}}" ng-model="checkboxModel">' +
            '<span> {{checkboxLabel}}</span>' +
            '</label>' +
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
            '<label>{{selectLabel}}</label>' +
            '<select class="form-control" multiple name="{{selectName}}" ng-model="selectModel" ng-options="item for item in selectItems" ></select>' +
            '</div>'
        };
    });
