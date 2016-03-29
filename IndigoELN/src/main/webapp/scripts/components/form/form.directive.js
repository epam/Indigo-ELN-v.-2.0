/* globals $ */
'use strict';
angular.module('indigoeln')
    .factory('formUtils', function ($timeout) {
        return {
            doVertical: function (tAttrs, tElement) {
                if (tAttrs.myLabelVertical && tAttrs.myLabel) {
                    tElement.find('.col-xs-2').removeClass('col-xs-2');
                    tElement.find('.col-xs-10').children().unwrap();
                    tElement.children().wrap('<div class="col-xs-12"/>');
                }
            },
            clearLabel: function (tAttrs, tElement) {
                if (!tAttrs.myLabel) {
                    tElement.find('label').remove();
                    tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-12');
                }
            },
            showValidation: function ($formGroup, scope) {
                $timeout(function () {
                    if (scope.myValidationObj) {
                        var $inputs = $formGroup.find('input[ng-model],textarea[ng-model],select[ng-model]');
                        if ($inputs.length > 0) {
                            $inputs.each(function () {
                                scope.$watch(function () {
                                    return scope.myValidationObj.$invalid && scope.myValidationObj.$dirty;
                                }, function (isInvalid) {
                                    $formGroup.toggleClass('has-error', isInvalid);
                                });
                            });
                        }
                    }
                });
            }
        };
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
            myInputSize: '@',
            myValidationObj: '=',
            myValidationRequired: '=',
            myValidationMaxlength: '@',
            myValidationMinlength: '@',
            myValidationPattern: '@',
            myValidationPatternText: '@',
            myClasses: '@'
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
            if (tAttrs.myInputGroup) {
                var elementIg = $('<div class="input-group"/>');
                if (tAttrs.myInputSize) {
                    elementIg.addClass('input-group-' + tAttrs.myInputSize);
                }
                var inputGroup = tElement.find('input').wrap(elementIg).parent();
                if (tAttrs.myInputGroup === 'append') {
                    inputGroup.append('<div class="input-group-btn" ng-transclude/>');
                } else if (tAttrs.myInputGroup === 'prepend') {
                    inputGroup.prepend('<div class="input-group-btn" ng-transclude/>');
                }
            }
            formUtils.clearLabel(tAttrs, tElement);
            if (tAttrs.myValidationMinlength) {
                tElement.find('input').attr('ng-minlength', tAttrs.myValidationMinlength);
            }
            if (tAttrs.myValidationMaxlength) {
                tElement.find('input').attr('ng-maxlength', tAttrs.myValidationMaxlength);
            }
            if (tAttrs.myValidationPattern) {
                tElement.find('input').attr('ng-pattern', tAttrs.myValidationPattern);
            }
            if (tAttrs.myValidationRequired) {
                tElement.find('input').attr('ng-required', tAttrs.myValidationRequired);
            }
            return {
                post: function postLink(scope, iElement, iAttrs, controller) {
                    formUtils.showValidation(iElement, scope);
                }
            };
        },
        template: '<div class="form-group {{myClasses}}">' +
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
            myName: '@',
            myClasses: '@',
            myDisabled: '='
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<div class="col-xs-offset-2 col-xs-10">' +
        '<div class="checkbox">' +
        '<label>' +
        '<input type="checkbox" id="{{myName}}" ng-model="myModel" ng-disabled="myDisabled"> {{myLabel}}' +
        '</label> ' +
        '</div> ' +
        '</div> ' +
        '</div> '
    };
}).directive('mySelect', function (formUtils, Dictionary) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myItems: '=',
            myDictionary: '@',
            myMultiple: '=',
            myLabelVertical: '=',
            myPlaceHolder: '@',
            myItemProp: '@',
            myOrderByProp: '@',
            myClasses: '@',
            myOnSelect: '&',
            myReadonly: '='
        },
        controller: function ($scope) {
            $scope.ctrl = {selected: $scope.myModel};
            $scope.$watchCollection('ctrl.selected', function (newSelected) {
                $scope.myModel = newSelected;
            });
            $scope.$watchCollection('myModel', function (myModel) {
                $scope.ctrl.selected = myModel;
            });
            if ($scope.myDictionary) {
                Dictionary.get({id: $scope.myDictionary}, function(dictionary) {
                    $scope.myItems = dictionary.words;
                });
            }
        },
        compile: function (tElement, tAttrs, transclude) {
            tAttrs.myItemProp = tAttrs.myItemProp || 'name';
            tAttrs.myOrderByProp = tAttrs.myOrderByProp || 'rank';
            if (tAttrs.myMultiple) {
                tElement.find('ui-select').attr('multiple', true);
                tElement.find('ui-select-match').html('{{$item.' + tAttrs.myItemProp + '}}');
            }
            formUtils.doVertical(tAttrs, tElement);
            var select = tElement.find('ui-select-choices');
            select.append('<span ng-bind-html="item.' + tAttrs.myItemProp +
                ' | highlight: $select.search"></span>');
            var repeat = select.attr('repeat');
            select.attr('repeat', repeat + ' | orderBy:"' + tAttrs.myOrderByProp + '"');
            formUtils.clearLabel(tAttrs, tElement);
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<ui-select ng-model="ctrl.selected" theme="bootstrap" ng-disabled="myReadonly" on-select="myOnSelect()">' +
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
            myLabelVertical: '=',
            myClasses: '@',
            myReadonly: '='
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
            if (tAttrs.myLabelVertical) {
                $('<br/>').insertAfter(tElement.find('label').first());
            }
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<div class="btn-group">' +
        '<label class="btn btn-info" ng-model="myModel" uib-btn-radio="myFirst" uncheckable ng-disabled="myReadonly">{{myFirst}}</label>' +
        '<label class="btn btn-info" ng-model="myModel" uib-btn-radio="mySecond" uncheckable ng-disabled="myReadonly">{{mySecond}}</label>' +
        '</div>' +
        '</div> ' +
        '</div> '
    };
}).directive('myTextArea', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myLabelVertical: '=',
            myClasses: '@',
            myInputGroup: '@',
            myReadonly: '=',
            myRowsNum: '='
        },
        compile: function (tElement, tAttrs, transclude) {
            if (tAttrs.myInputGroup) {
                var inputGroup = tElement.find('textarea').wrap('<div class="input-group"/>').parent();
                var element = '<div class="input-group-btn" style="vertical-align: top;" ng-transclude/>';
                if (tAttrs.myInputGroup === 'append') {
                    inputGroup.append(element);
                } else if (tAttrs.myInputGroup === 'prepend') {
                    inputGroup.prepend(element);
                }
            }
            if (tAttrs.myRowsNum) {
                tElement.find('textarea').attr('rows', tAttrs.myRowsNum);
            }
            formUtils.doVertical(tAttrs, tElement);
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label> ' +
        '<div class="col-xs-10">' +
        '<textarea class="form-control" rows="1" ng-model="myModel" ng-readonly="myReadonly" msd-elastic style="resize: none;"></textarea>' +
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
            myEmptyText: '@',
            myClasses: '@'
        },
        compile: function (tElement, tAttrs, transclude) {
            //formUtils.doVertical(tAttrs, tElement);
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<div class="col-xs-12 text-left" style="padding-top: 7px">' +
        '<div style="float: left; width: 150px"><b>{{myLabel}}</b></div> <span>{{myModel||myEmptyText}}</span>' +
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
            myValidationRequired: '=',
            myClasses: '@'
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
            tElement.find('input').attr('timezone', jstz.determine().name());
            return {
                post: function postLink(scope, iElement, iAttrs, controller) {
                    if (scope.myModel) {
                        scope.ctrl = {};
                        scope.ctrl.model = moment(scope.myModel);
                        scope.$watch('ctrl.model', function (date) {
                            scope.myModel = date ? date.toISOString() : null;
                        });
                    }
                    formUtils.showValidation(iElement, scope);
                }
            };
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        '<input type="{{myType}}" class="form-control" name="{{myName}}" ng-model="ctrl.model" date-time view="date" ' +
        'format="MMM DD, YYYY HH:mm:ss z" ng-disabled="myReadonly" ng-required="myValidationRequired"/>' +
        '<div ng-show="myValidationObj.$invalid">' +
        '<p class="help-block" ng-show="myValidationObj.$error.required"> This field is required. </p>' +
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
            myReadonly: '=',
            myClasses: '@'
        },
        compile: function (tElement, tAttrs, transclude) {
            formUtils.doVertical(tAttrs, tElement);
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label class="col-xs-2 control-label">{{myLabel}}</label>' +
        '<div class="col-xs-10">' +
        ' <tags-input ng-model="myModel" ng-disabled="myReadonly"></tags-input>' +
        '</div>' +
        '</div>'
    };
});
