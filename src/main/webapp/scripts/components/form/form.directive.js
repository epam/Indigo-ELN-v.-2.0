/* globals $ */
'use strict';
angular.module('indigoeln')
    .factory('formUtils', function ($timeout) {
        return {
            doVertical: function (tAttrs, tElement) {
                if (tAttrs.myLabelVertical) {
                    tElement.find('.col-xs-2').removeClass('col-xs-2');
                    tElement.find('.col-xs-10').children().unwrap();
                    tElement.children().wrap('<div class="col-xs-12"/>');
                }
            },
            showValidation: function ($formGroup, scope) {
                if (scope.myValidationObj) {
                    var $inputs = $formGroup.find('input[ng-model],textarea[ng-model],select[ng-model]');
                    if ($inputs.length > 0) {
                        $inputs.each(function () {
                            var $input = $(this);
                            scope.$watch(function () {
                                return scope.myValidationObj.$invalid && scope.myValidationObj.$dirty
                            }, function (isInvalid) {
                                $formGroup.toggleClass('has-error', isInvalid);
                            });
                        });
                    }
                }
            }
        }
    }).directive('myInput', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            myLabel: '@',
            myLabelVertical: '=',
            myName: '@',
            myModel: '=',
            myReadonly: '=',
            myType: '@',
            myInputGroup: '@',
            myValidationObj: '=',
            myValidationRequired: '=',
            myValidationMaxlength: '@',
            myValidationMinlength: '@',
            myValidationPattern: '@',
            myValidationPatternText: '@'
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
            if (tAttrs.myInputGroup) {
                var inputGroup = tElement.find('input').wrap('<div class="input-group"/>').parent();
                if (tAttrs.myInputGroup == 'append') {
                    inputGroup.append('<div class="input-group-btn" ng-transclude/>')
                } else if (tAttrs.myInputGroup == 'prepend') {
                    inputGroup.prepend('<div class="input-group-btn" ng-transclude/>')
                }
            }
            if (tAttrs.myValidationMinlength) {
                tElement.find('input').attr("ng-minlength", tAttrs.myValidationMinlength)
            }
            if (tAttrs.myValidationMaxlength) {
                tElement.find('input').attr("ng-maxlength", tAttrs.myValidationMaxlength)
            }
            if (tAttrs.myValidationPattern) {
                tElement.find('input').attr("ng-pattern", tAttrs.myValidationPattern)
            }
            if (tAttrs.myValidationRequired) {
                tElement.find('input').attr("ng-required", tAttrs.myValidationRequired)
            }
            return {
                post: function postLink(scope, iElement, iAttrs, controller) {
                    formUtils.showValidation(iElement, scope)
                }
            }
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<input type="{{myType}}" class="form-control" name="{{myName}}" ng-model="myModel" ng-readonly="myReadonly"/>' +
        '<div ng-show="myValidationObj.$invalid">' +
        '<p class="help-block" ng-if="myValidationObj.$error.required"> This field is required. </p>' +
        '<p class="help-block" ng-if="myValidationObj.$error.maxlength" > This field can\'t be longer than {{myValidationMaxlength}} characters.</p>' +
        '<p class="help-block" ng-if="myValidationObj.$error.pattern" >{{myValidationPatternText}}</p>' +
        '</div>' +
        '</div>' +
        '</div>'
    };
}).directive('myCheckbox', function () {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myName: '@'
        },
        template: '<div class="form-group">' +
        '<div class="col-xs-offset-2 col-xs-10">' +
        '<div class="checkbox">' +
        '<label>' +
        '<input type="checkbox" id="{{myName}}" ng-model="myModel"> {{myLabel}}' +
        '</label> ' +
        '</div> ' +
        '</div> ' +
        '</div> '
    };
}).directive('mySelect', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myItems: '=',
            myMultiple: '=',
            myLabelVertical: '=',
            myPlaceHolder: '@',
            myItemProp: '@'
        },
        controller: function ($scope) {
            $scope.ctrl = {selected: $scope.myModel};
            $scope.$watchCollection('ctrl.selected', function (newSelected) {
                $scope.myModel = newSelected;
            });
        },
        compile: function (tElement, tAttrs, transclude) {
            tAttrs.myItemProp = tAttrs.myItemProp || 'name';
            if (tAttrs.myMultiple) {
                tElement.find('ui-select').attr("multiple", true);
                tElement.find('ui-select-match').html('{{$item.' + tAttrs.myItemProp + '}}');
            }
            formUtils.doVertical(tAttrs, tElement);
            tElement.find('ui-select-choices').append('<span ng-bind-html="item.' + tAttrs.myItemProp +
                ' | highlight: $select.search"></span>')
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<ui-select ng-model="ctrl.selected" theme="bootstrap" ng-disabled="disabled">' +
        '<ui-select-match placeholder="{{myPlaceHolder}}"> {{$select.selected.name}}</ui-select-match>' +
        '<ui-select-choices repeat="item in myItems | filter: $select.search">' +
        '</ui-select-choices>' +
        '</ui-select>' +
        '</div>' +
        '</div>'
    };
}).directive('myTwoToggle', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myFirst: '@',
            mySecond: '@',
            myLabelVertical: '='
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
            if (tAttrs.myLabelVertical) {
                $("<br/>").insertAfter(tElement.find('label').first());
            }
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<div class="btn-group">' +
        '<label class="btn btn-info" ng-model="myModel" uib-btn-radio="myFirst" uncheckable>{{myFirst}}</label>' +
        '<label class="btn btn-info" ng-model="myModel" uib-btn-radio="mySecond" uncheckable>{{mySecond}}</label>' +
        '</div>' +
        '</div> ' +
        '</div> '
    };
}).directive('myTextArea', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myLabelVertical: '='
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label> ' +
        '<div class="col-xs-10">' +
        '<textarea class="form-control" rows="3" ng-model="myModel"></textarea>' +
        '</div> ' +
        '</div> '
    };
}).directive('mySimpleText', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myEmptyText: '@'
        },
        compile: function (tElement, tAttrs, transclude) {
            //formUtils.doVertical(tAttrs, tElement);
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">' +
        '{{myLabel}} ' +
        '</label>' +
        '<div class="col-xs-10" style="padding-top: 7px;">' +
        '{{myModel||myEmptyText}}' +
        '</div>' +
        '</div>'
    };
}).directive('myDatePicker', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myLabelVertical: '=',
            myName: '@',
            myModel: '=',
            myReadonly: '=',
            myType: '@',
            myValidationObj: '=',
            myValidationRequired: '='
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
            return {
                post: function postLink(scope, iElement, iAttrs, controller) {
                    formUtils.showValidation(iElement, scope)
                }
            }
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<div class="input-group">' +
        '<input type="{{myType}}" class="form-control" name="{{myName}}" ng-model="myModel" uib-datepicker-popup="myTzAbbr" is-open="isOpen" ng-readonly="myReadonly" ng-required="myValidationRequired" ng-maxlength="myValidationMaxlength"/>' +
        '<span class="input-group-btn">' +
        '<button type="button" ng-disabled="myReadonly" class="btn btn-default" ng-click="isOpen = !isOpen"><i class="glyphicon glyphicon-calendar"></i></button></span>' +
        '<div ng-show="myValidationObj.$invalid">' +
        '<p class="help-block" ng-show="myValidationObj.$error.required"> This field is required. </p>' +
        '</div>' +
        '</div>' +
        '</div>' +
        '</div>'
    };
}).directive('myTagInput', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myLabelVertical: '=',
            myModel: '=',
            myReadonly: '='
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
        },
        template: '<div class="form-group">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        ' <tags-input ng-model="myModel" ng-disabled="myReadonly"></tags-input>' +
        '</div>' +
        '</div>'
    };
});
