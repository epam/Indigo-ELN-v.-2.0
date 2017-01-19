/* globals $ */
angular.module('indigoeln')
    .factory('formUtils', function ($timeout) {
        return {
            doVertical: function (tAttrs, tElement) {
                if (tAttrs.myLabelVertical && tAttrs.myLabel) {
                    tElement.find('.col-xs-2').removeClass('col-xs-2');
                    tElement.find('.col-xs-10').children().unwrap();
                    // tElement.children().wrap('<div class="col-xs-12"/>');
                }
            },
            clearLabel: function (tAttrs, tElement) {
                if (!tAttrs.myLabel) {
                    tElement.find('label').remove();
                    tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-12');
                }
            },
            setLabelColumns: function (tAttrs, tElement) {
                if (tAttrs.myLabelColumnsNum) {
                    var labelColumnsNum = parseInt(tAttrs.myLabelColumnsNum, 10);
                    if (_.isNumber(labelColumnsNum) && labelColumnsNum > 0 && labelColumnsNum < 12) {
                        tElement.find('label').removeClass('col-xs-2').addClass('col-xs-' + labelColumnsNum);
                        tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-' + (12 - labelColumnsNum));
                    }
                }
            },
            showValidation: function ($formGroup, scope, formCtrl) {
                $timeout(function () {
                    if (formCtrl) {
                        var ngModelCtrl = formCtrl[scope.myName];
                        if (ngModelCtrl) {
                            scope.ngModelCtrl = ngModelCtrl;
                            var $inputs = $formGroup.find('input[ng-model],textarea[ng-model],select[ng-model]');
                            if ($inputs.length > 0) {
                                var unbinds = [];
                                $inputs.each(function () {
                                    unbinds.push(scope.$watch(function () {
                                        return ngModelCtrl.$invalid && (ngModelCtrl.$dirty || formCtrl.$submitted);
                                    }, function (isInvalid) {
                                        $formGroup.toggleClass('has-error', isInvalid);
                                    }));
                                });
                                scope.$on('$destroy', function () {
                                    _.each(unbinds, function (unbind) {
                                        unbind();
                                    });
                                });
                            }
                        }
                    }
                }, 0, false);
            },
            addOnChange: function (scope) {
                if (scope.myChange) {
                    scope.myChangeAsync = function () {
                        $timeout(scope.myChange, 0, false);
                    };
                }
                if (scope.myClick) {
                    scope.myClickAsync = function () {
                        $timeout(scope.myClick, 0, false);
                    };
                }

            },
            addDirectivesByAttrs: function (tAttrs, $element) {
                if (tAttrs.myValidationMinlength) {
                    $element.attr('ng-minlength', tAttrs.myValidationMinlength);
                }
                if (tAttrs.myValidationMaxlength) {
                    $element.attr('ng-maxlength', tAttrs.myValidationMaxlength);
                }
                if (tAttrs.myValidationPattern) {
                    $element.attr('ng-pattern', tAttrs.myValidationPattern);
                }
                if (tAttrs.myValidationRequired) {
                    $element.attr('ng-required', tAttrs.myValidationRequired);
                }
                if (tAttrs.myChange) {
                    $element.attr('ng-change', 'myChangeAsync()');
                }
                if (tAttrs.myClick) {
                    $element.attr('ng-click', 'myClickAsync()');
                }
                if (tAttrs.myTooltip) {
                    $element.attr('uib-popover', '{{myTooltip}}')
                        .attr('popover-trigger', 'mouseenter')
                        .attr('popover-placement', 'bottom');
                }
                if (tAttrs.myModel) {
                    $element.attr('ng-model', 'myModel')
                        .attr('ng-model-options', '{ debounce: 150 }');
                }
                if (tAttrs.myParsers || tAttrs.myFormatters) {
                    $element.attr('my-parsers-formatters', '{myParsers: myParsers, myFormatters: myFormatters}');
                }

                if (tAttrs.myReadonly) {
                    $element.attr('ng-readonly', 'myReadonly');
                }
                if (tAttrs.myName) {
                    $element.attr('name', '{{myName}}');
                }
                if (tAttrs.myType) {
                    $element.attr('type', '{{myType}}');
                }
                if (tAttrs.myDisabled) {
                    $element.attr('ng-disabled', 'myDisabled');
                }

            }
        };
    }).directive('myInput', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        require: '?^form',
        scope: {
            myLabel: '@',
            myLabelVertical: '=',
            myLabelColumnsNum: '=',
            myName: '@',
            myModel: '=',
            myReadonly: '=',
            myType: '@',
            myInputGroup: '@',
            myInputSize: '@',
            myChange: '&',
            myClick: '&',
            myValidationRequired: '=',
            myValidationMaxlength: '@',
            myValidationMinlength: '@',
            myValidationPattern: '@',
            myValidationPatternText: '@',
            myClasses: '@',
            myParsers: '=',
            myFormatters: '=',
            myTooltip: '='
        },
        compile: function (tElement, tAttrs) {
            formUtils.doVertical(tAttrs, tElement);
            var $input = tElement.find('input');
            if (tAttrs.myInputGroup) {
                var elementIg = $('<div class="input-group"/>');
                if (tAttrs.myInputSize) {
                    elementIg.addClass('input-group-' + tAttrs.myInputSize);
                }
                var inputGroup = $input.wrap(elementIg).parent();
                if (tAttrs.myInputGroup === 'append') {
                    inputGroup.append('<div class="input-group-btn" ng-transclude/>');
                } else if (tAttrs.myInputGroup === 'prepend') {
                    inputGroup.prepend('<div class="input-group-btn" ng-transclude/>');
                }
            }
            formUtils.clearLabel(tAttrs, tElement);
            formUtils.setLabelColumns(tAttrs, tElement);
            formUtils.addDirectivesByAttrs(tAttrs, $input);
            return {
                post: function (scope, iElement, iAttrs, formCtrl) {
                    formUtils.showValidation(iElement, scope, formCtrl);
                    formUtils.addOnChange(scope);
                }
            };
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label>{{myLabel}}</label>' +
        // '<div class="col-xs-10">' +
        '<input class="form-control"/>' +
        '<div ng-show="ngModelCtrl.$invalid">' +
        '<p class="help-block" ng-if="ngModelCtrl.$error.required"> This field is required. </p>' +
        '<p class="help-block" ng-if="ngModelCtrl.$error.maxlength" > This field can\'t be longer than {{myValidationMaxlength}} characters.</p>' +
        '<p class="help-block" ng-if="ngModelCtrl.$error.pattern" >{{myValidationPatternText}}</p>' +
        // '</div>' +
        '</div>' +
        '</div>'
    };
}).directive('myParsersFormatters', function () {
    return {
        restrict: 'A',
        require: 'ngModel',
        scope: {
            myParsersFormatters: '='
        },
        link: function (scope, element, attrs, ngModel) {
            //model -> view
            _.each(scope.myParsersFormatters.myFormatters, function (i) {
                ngModel.$formatters.push(i);
            });
            //view -> model
            _.each(scope.myParsersFormatters.myParsers, function (i) {
                ngModel.$parsers.push(i);
            });
        }
    };
}).directive('myCheckbox', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myName: '@',
            myClasses: '@',
            myDisabled: '=',
            myChange: '&',
            myClick: '&',
            myTooltip: '@',
            myTooltipPlacement: '@'
        },
        compile: function (tElement, tAttrs) {
            formUtils.clearLabel(tAttrs, tElement);
            var $checkbox = tElement.find('checkbox');
            formUtils.addDirectivesByAttrs(tAttrs, $checkbox);
            if (tAttrs.myModel) {
                $checkbox.removeAttr('ng-model-options');
            }
            return {
                post: function (scope) {
                    formUtils.addOnChange(scope);
                }
            };
        },
        template: '<div class="my-checkbox-wrapper form-group {{myClasses}}">' +
        '<div class="checkbox">' +
        '<checkbox id="{{myName}}" class="btn-info my-checkbox"></checkbox> ' +
        '<label uib-tooltip="{{myTooltip}}" tooltip-placement="{{myTooltipPlacement}}" for="{{myName}}" ng-click="!myDisabled ? myModel = !myModel : return; myChangeAsync();">{{myLabel}}</label>' +
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
            myLabelColumnsNum: '=',
            myControl: '=',
            myPlaceHolder: '@',
            myItemProp: '@',
            myOrderByProp: '@',
            myClasses: '@',
            myChange: '&',
            myRemove: '&',
            myReadonly: '='
        },
        controller: function ($scope) {
            $scope.ctrl = {selected: $scope.myModel};

            $scope.control = $scope.myControl || {};

            $scope.control.setSelection = function(select){
                $scope.ctrl.selected = select;
            };

            $scope.control.unSelect = function(){
                $scope.ctrl.selected = {};
            };

            $scope.$watchCollection('ctrl.selected', function (newSelected) {
                $scope.myModel = newSelected;
            });
            $scope.$watchCollection('myModel', function (myModel) {
                $scope.ctrl.selected = myModel;
            });
            if ($scope.myDictionary) {
                Dictionary.getByName({name: $scope.myDictionary}, function (dictionary) {
                    $scope.myItems = dictionary.words;
                });
            }
        },
        compile: function (tElement, tAttrs) {
            tAttrs.myItemProp = tAttrs.myItemProp || 'name';
            tAttrs.myOrderByProp = tAttrs.myOrderByProp || 'rank';
            if (tAttrs.myMultiple) {
                tElement.find('ui-select').attr('multiple', true);
                tElement.find('ui-select-match').html('{{$item.' + tAttrs.myItemProp + '}}');
            }
            formUtils.doVertical(tAttrs, tElement);
            var select = tElement.find('ui-select-choices');
            var htmlContent = _.reduce(tAttrs.myItemProp.split(','), function (memo, num) {
                return memo + (memo.length > 0 ? " + ' - ' + " : '') + 'item.' + num;
            }, '');
            select.append('<span ng-bind-html="' + htmlContent + ' | highlight: $select.search"></span>');
            var repeat = select.attr('repeat');
            select.attr('repeat', repeat + ' | orderBy:"' + tAttrs.myOrderByProp + '"');
            formUtils.clearLabel(tAttrs, tElement);
            formUtils.setLabelColumns(tAttrs, tElement);
            return {
                post: function (scope) {
                    formUtils.addOnChange(scope);
                }
            };
        },
        template: function (tElement, tAttrs) {
            var itemProp = tAttrs.myItemProp || 'name';
            var content = _.chain(itemProp.split(','))
                .map(function (prop) {
                    return '{{ $select.selected.' + prop + '}}';
                })
                .reduce(function (memo, num) {
                    return memo + (memo.length > 0 ? ' - ' : '') + num;
                }, '').value();
            return '<div class="form-group {{myClasses}}">' +
                '<label>{{myLabel}}</label>' +
                // '<div class="col-xs-10">' +
                '<ui-select ng-model="ctrl.selected" theme="bootstrap" ng-disabled="myReadonly" on-select="myChange()" on-remove="myRemove()" append-to-body="true">' +
                '<ui-select-match placeholder="{{myPlaceHolder}}" >' + content + '</ui-select-match>' +
                '<ui-select-choices repeat="item in myItems | filter: $select.search">' +
                '</ui-select-choices>' +
                '</ui-select>' +
                // '</div>' +
                '</div>';
        }
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
        compile: function (tElement, tAttrs) {
            formUtils.doVertical(tAttrs, tElement);
            if (tAttrs.myLabelVertical) {
                $('<br/>').insertAfter(tElement.find('label').first());
            }
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label class="m-r5">{{myLabel}}</label>' +
        // '<div class="col-xs-10">' +
        '<div class="btn-group">' +
        '<label class="btn btn-info" ng-model-options="{ debounce: 150 }" ng-model="myModel" uib-btn-radio="myFirst" uncheckable ng-disabled="myReadonly">{{myFirst}}</label>' +
        '<label class="btn btn-info" ng-model-options="{ debounce: 150 }" ng-model="myModel" uib-btn-radio="mySecond" uncheckable ng-disabled="myReadonly">{{mySecond}}</label>' +
        // '</div>' +
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
            myNoElastic: '=',
            myInputGroup: '@',
            myReadonly: '=',
            myRowsNum: '=',
            myTooltip: '=',
            myTrim: '='
        },
        compile: function (tElement, tAttrs) {
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
            formUtils.addDirectivesByAttrs(tAttrs, tElement.find('textarea'));

            if (tAttrs.myTrim) {
                tElement.find('textarea').attr('ng-trim', tAttrs.myTrim);
            }

            if (tAttrs.myNoElastic){
                tElement.find('textarea').removeAttr('msd-elastic');
            }

        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label>{{myLabel}}</label> ' +
        // '<div class="col-xs-10">' +
        '<textarea class="form-control" rows="1" msd-elastic  style="resize: none;"></textarea>' +
        // '</div> ' +
        '</div> '
    };
}).directive('mySimpleText', function () {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myLabel: '@',
            myModel: '=',
            myEmptyText: '@',
            myClasses: '@'
        },
        compile: function () {
        },
        template: '<div class="form-group {{myClasses}}">' +
        // '<div class="col-xs-12 text-left">' +
        '<p><span class="semi-b">{{myLabel}}:</span>&nbsp;&nbsp;<span>{{myModel||myEmptyText}}</span></p>' +
        // '</div>' +
        '</div>'
    };
}).directive('myDatePicker', function (formUtils) {
    return {
        restrict: 'E',
        replace: true,
        require: '?^form',
        scope: {
            myLabel: '@',
            myLabelVertical: '=',
            myName: '@',
            myModel: '=',
            myReadonly: '=',
            myType: '@',
            myValidationRequired: '=',
            myClasses: '@'
        },
        compile: function (tElement, tAttrs) {
            formUtils.doVertical(tAttrs, tElement);
            tElement.find('input').attr('timezone', jstz.determine().name());
            return {
                post: function (scope, iElement, iAttrs, formCtrl) {
                    if (scope.myModel) {
                        scope.ctrl = {};
                        scope.ctrl.model = moment(scope.myModel);
                        var unsubscribe = scope.$watch('ctrl.model', function (date) {
                            scope.myModel = date ? date.toISOString() : null;
                        });
                        scope.$on('$destroy', function () {
                            unsubscribe();
                        });
                    }
                    formUtils.showValidation(iElement, scope, formCtrl);
                }
            };
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label>{{myLabel}}</label>' +
        // '<div class="col-xs-10">' +
        '<input type="{{myType}}" class="form-control" name="{{myName}}" ng-model-options="{ debounce: 150 }" ng-model="ctrl.model" date-time view="date" ' +
        'format="MMM DD, YYYY HH:mm:ss z" ng-disabled="myReadonly" ng-required="myValidationRequired"/>' +
        '<div ng-show="ngModelCtrl.$invalid">' +
        '<p class="help-block" ng-show="ngModelCtrl.$error.required"> This field is required. </p>' +
        // '</div>' +
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
            myClasses: '@',
            myOnClick: '=',
            myOnAdding: '=',
            myPlaceholder: '@',
            myMaxTags: '=',
            mySource: '='
        },
        compile: function (tElement, tAttrs) {
            formUtils.doVertical(tAttrs, tElement);
            var $tagInput = tElement.find('tags-input');
            if (tAttrs.mySource) {
                var autoComplete = '<auto-complete min-length="1" source="mySource($query)">';
                $tagInput.append(autoComplete);
            }
            formUtils.doVertical(tAttrs, tElement);
            formUtils.addDirectivesByAttrs(tAttrs, $tagInput);
            if (tAttrs.myOnClick) {
                $tagInput.attr('on-tag-clicked', 'myOnClick($tag)');
            }
            if (tAttrs.myOnAdding) {
                $tagInput.attr('on-tag-adding', 'myOnAdding($tag)');
            }
            if (tAttrs.myMaxTags) {
                $tagInput.attr('max-tags', '{{myMaxTags}}');
            }
            if (tAttrs.myMaxTags) {
                $tagInput.attr('placeholder', '{{myPlaceholder}}');
            }
        },
        template: '<div class="form-group {{myClasses}}">' +
        '<label>{{myLabel}}</label>' +
        '<div>' +
        ' <tags-input replace-spaces-with-dashes="false"></tags-input>' +
        '</div>' +
        '</div>'
    };
}).directive('myChecklist', function () {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            myItems: '=',
            myLabel: '@'
        },
        controller: function ($scope) {
            $scope.allItemsSelected = false;
            $scope.selectAll = function () {
                for (var i = 0; i < $scope.myItems.length; i++) {
                    $scope.myItems[i].isChecked = $scope.allItemsSelected;
                }
            };
        },
        template: '<div class="row"><div class="col-xs-3">{{myLabel}}:</div>' +
        '<div class="col-xs-9 form-inline"><my-checkbox my-model="allItemsSelected" ' +
        'my-change="selectAll()" my-label="All"></my-checkbox>' +
        '<my-checkbox ng-repeat="item in myItems" my-label="{{item.value}}" ' +
        'my-model="item.isChecked" ></my-checkbox></div></div>'
    };
});
