/* globals $ */
(function () {
    angular
        .module('indigoeln')
        .factory('formUtils', function ($timeout) {
            return {
                doVertical: function (tAttrs, tElement) {
                    if (tAttrs.indigoLabelVertical && tAttrs.indigoLabel) {
                        tElement.find('.col-xs-2').removeClass('col-xs-2');
                        tElement.find('.col-xs-10').children().unwrap();
                        // tElement.children().wrap('<div class="col-xs-12"/>');
                    }
                },
                clearLabel: function (tAttrs, tElement) {
                    if (!tAttrs.indigoLabel) {
                        tElement.find('label').remove();
                        tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-12');
                    }
                },
                setLabelColumns: function (tAttrs, tElement) {
                    if (tAttrs.indigoLabelColumnsNum) {
                        var labelColumnsNum = parseInt(tAttrs.indigoLabelColumnsNum, 10);
                        if (_.isNumber(labelColumnsNum) && labelColumnsNum > 0 && labelColumnsNum < 12) {
                            tElement.find('label').removeClass('col-xs-2').addClass('col-xs-' + labelColumnsNum);
                            tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-' + (12 - labelColumnsNum));
                        }
                    }
                },
                showValidation: function ($formGroup, scope, formCtrl) {
                    $timeout(function () {
                        if (formCtrl) {
                            var ngModelCtrl = formCtrl[scope.indigoName];
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
                    if (scope.indigoChange) {
                        scope.indigoChangeAsync = function () {
                            $timeout(scope.indigoChange, 0, false);
                        };
                    }
                    if (scope.indigoClick) {
                        scope.indigoClickAsync = function () {
                            $timeout(scope.indigoClick, 0, false);
                        };
                    }

                },
                addDirectivesByAttrs: function (tAttrs, $element) {
                    if (tAttrs.indigoValidationMinlength) {
                        $element.attr('ng-minlength', tAttrs.indigoValidationMinlength);
                    }
                    if (tAttrs.indigoValidationMaxlength) {
                        $element.attr('ng-maxlength', tAttrs.indigoValidationMaxlength);
                    }
                    if (tAttrs.indigoValidationPattern) {
                        $element.attr('ng-pattern', tAttrs.indigoValidationPattern);
                    }
                    if (tAttrs.indigoValidationRequired) {
                        $element.attr('ng-required', tAttrs.indigoValidationRequired);
                    }
                    if (tAttrs.indigoChange) {
                        $element.attr('ng-change', 'indigoChangeAsync()');
                    }
                    if (tAttrs.indigoClick) {
                        $element.attr('ng-click', 'indigoClickAsync()');
                    }
                    /*if (tAttrs.indigoTooltip) {
                     $element.attr('uib-popover', '{{indigoTooltip}}')
                     .attr('popover-trigger', 'mouseenter')
                     .attr('popover-placement', 'bottom');
                     }*/
                    if (tAttrs.indigoModel) {
                        $element.attr('ng-model', 'indigoModel')
                            .attr('ng-model-options', '{ debounce: 150 }');
                    }
                    if (tAttrs.indigoParsers || tAttrs.indigoFormatters) {
                        $element.attr('indigo-parsers-formatters', '{indigoParsers: indigoParsers, indigoFormatters: indigoFormatters}');
                    }

                    if (tAttrs.indigoReadonly) {
                        $element.attr('ng-readonly', 'indigoReadonly');
                    }
                    if (tAttrs.indigoName) {
                        $element.attr('name', '{{indigoName}}');
                    }
                    if (tAttrs.indigoNumberMin != undefined) {
                        $element.attr('min', tAttrs.indigoNumberMin);
                    }
                    if (tAttrs.indigoType) {
                        $element.attr('type', '{{indigoType}}');
                    }
                    if (tAttrs.indigoDisabled) {
                        $element.attr('ng-disabled', 'indigoDisabled');
                    }

                }
            };
        })
        .directive('indigoInput', indigoInput)
        .directive('indigoParsersFormatters', indigoParsersFormatters)
        .directive('noDirty', noDirty)
        .directive('indigoCheckbox', indigoCheckbox)
        .directive('indigoSelect', indigoSelect)
        .directive('indigoTwoToggle', indigoTwoToggle)
        .directive('indigoTextArea', indigoTextArea)
        .directive('indigoSimpleText', indigoSimpleText)
        .directive('indigoDatePicker', indigoDatePicker)
        .directive('indigoTagInput', indigoTagInput)
        .directive('indigoChecklist', indigoChecklist);

    function indigoChecklist() {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoItems: '=',
                indigoLabel: '@'
            },
            controller: indigoChecklistController,
            template: '<div class="row"><div class="col-xs-3">{{indigoLabel}}:</div>' +
            '<div class="col-xs-9 form-inline"><indigo-checkbox indigo-model="allItemsSelected" ' +
            'indigo-change="selectAll()" indigo-label="All"></indigo-checkbox>' +
            '<indigo-checkbox ng-repeat="item in indigoItems" indigo-label="{{item.value}}" ' +
            'indigo-model="item.isChecked" ></indigo-checkbox></div></div>'
        };
    }

    /* @ngInject */
    function indigoChecklistController($scope) {
        $scope.allItemsSelected = false;
        $scope.selectAll = function () {
            for (var i = 0; i < $scope.indigoItems.length; i++) {
                $scope.indigoItems[i].isChecked = $scope.allItemsSelected;
            }
        };
    }

    /* @ngInject */
    function indigoTagInput(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoLabelVertical: '=',
                indigoModel: '=',
                indigoReadonly: '=',
                indigoClasses: '@',
                indigoOnClick: '=',
                indigoOnAdding: '=',
                indigoPlaceholder: '@',
                indigoMaxTags: '=',
                indigoSource: '='
            },
            compile: angular.bind({formUtils: formUtils}, indigoTagInputCompile),
            template: '<div class="form-group {{indigoClasses}}">' +
            '<label>{{indigoLabel}}</label>' +
            '<div>' +
            ' <tags-input replace-spaces-with-dashes="false"></tags-input>' +
            '</div>' +
            '</div>'
        };
    }

    /* @ngInject */
    function indigoTagInputCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        formUtils.doVertical(tAttrs, tElement);
        var $tagInput = tElement.find('tags-input');
        if (tAttrs.indigoSource) {
            var autoComplete = '<auto-complete min-length="1" source="indigoSource($query)">';
            $tagInput.append(autoComplete);
        }
        formUtils.doVertical(tAttrs, tElement);
        formUtils.addDirectivesByAttrs(tAttrs, $tagInput);
        if (tAttrs.indigoOnClick) {
            $tagInput.attr('on-tag-clicked', 'indigoOnClick($tag)');
        }
        if (tAttrs.indigoOnAdding) {
            $tagInput.attr('on-tag-adding', 'indigoOnAdding($tag)');
        }
        if (tAttrs.indigoMaxTags) {
            $tagInput.attr('max-tags', '{{indigoMaxTags}}');
        }
        if (tAttrs.indigoMaxTags) {
            $tagInput.attr('placeholder', '{{indigoPlaceholder}}');
        }
    }

    /* @ngInject */
    function indigoDatePicker(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            require: '?^form',
            scope: {
                indigoLabel: '@',
                indigoLabelVertical: '=',
                indigoName: '@',
                indigoModel: '=',
                indigoReadonly: '=',
                indigoType: '@',
                indigoValidationRequired: '=',
                indigoClasses: '@'
            },
            compile: angular.bind({formUtils: formUtils}, indigoDatePickerCompile),
            template: '<div class="form-group {{indigoClasses}}">' +
            '<label>{{indigoLabel}}</label>' +
            // '<div class="col-xs-10">' +
            '<input type="{{indigoType}}" class="form-control" name="{{indigoName}}" ng-model-options="{ debounce: 150 }" ng-model="ctrl.model" date-time view="date" ' +
            'format="MMM DD, YYYY HH:mm:ss z" ng-disabled="indigoReadonly" ng-required="indigoValidationRequired"/>' +
            '<div ng-show="ngModelCtrl.$invalid">' +
            '<p class="help-block" ng-show="ngModelCtrl.$error.required"> This field is required. </p>' +
            // '</div>' +
            '</div>' +
            '</div>'
        };
    }

    /* @ngInject */
    function indigoDatePickerCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        formUtils.doVertical(tAttrs, tElement);
        tElement.find('input').attr('timezone', jstz.determine().name());
        return {
            post: function (scope, iElement, iAttrs, formCtrl) {
                if (scope.indigoModel) {
                    scope.ctrl = {};
                    scope.ctrl.model = moment(scope.indigoModel);
                    var unsubscribe = scope.$watch('ctrl.model', function (date) {
                        scope.indigoModel = date ? date.toISOString() : null;
                    });
                    scope.$on('$destroy', function () {
                        unsubscribe();
                    });
                }
                formUtils.showValidation(iElement, scope, formCtrl);
            }
        };
    }

    function indigoSimpleText() {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoEmptyText: '@',
                indigoClasses: '@'
            },
            compile: function () {
            },
            template: '<div class="form-group {{indigoClasses}}">' +
            // '<div class="col-xs-12 text-left">' +
            '<p><span class="semi-b">{{indigoLabel}}:</span>&nbsp;&nbsp;<span>{{indigoModel||indigoEmptyText}}</span></p>' +
            // '</div>' +
            '</div>'
        };
    }

    /* @ngInject */
    function indigoTextArea(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoLabelVertical: '=',
                indigoClasses: '@',
                indigoNoElastic: '=',
                indigoInputGroup: '@',
                indigoReadonly: '=',
                indigoRowsNum: '=',
                indigoTooltip: '=',
                indigoTrim: '='
            },
            compile: angular.bind({formUtils: formUtils}, indigoTextAreaCompile),
            template: '<div class="form-group {{indigoClasses}}">' +
            '<label>{{indigoLabel}}</label> ' +
            // '<div class="col-xs-10">' +
            '<textarea class="form-control" rows="1" msd-elastic  style="resize: none;"></textarea>' +
            // '</div> ' +
            '</div> '
        };
    }

    /* @ngInject */
    function indigoTextAreaCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        if (tAttrs.indigoInputGroup) {
            var inputGroup = tElement.find('textarea').wrap('<div class="input-group"/>').parent();
            var element = '<div class="input-group-btn" style="vertical-align: top;" ng-transclude/>';
            if (tAttrs.indigoInputGroup === 'append') {
                inputGroup.append(element);
            } else if (tAttrs.indigoInputGroup === 'prepend') {
                inputGroup.prepend(element);
            }
        }
        if (tAttrs.indigoRowsNum) {
            tElement.find('textarea').attr('rows', tAttrs.indigoRowsNum);
        }
        formUtils.doVertical(tAttrs, tElement);
        formUtils.addDirectivesByAttrs(tAttrs, tElement.find('textarea'));

        if (tAttrs.indigoTrim) {
            tElement.find('textarea').attr('ng-trim', tAttrs.indigoTrim);
        }

        if (tAttrs.indigoNoElastic) {
            tElement.find('textarea').removeAttr('msd-elastic');
        }

    }

    /* @ngInject */
    function indigoTwoToggle(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoFirst: '@',
                indigoSecond: '@',
                indigoLabelVertical: '=',
                indigoClasses: '@',
                indigoReadonly: '='
            },
            compile: angular.bind({formUtils: formUtils}, indigoTwoToggleCompile),
            template: '<div class="form-group {{indigoClasses}}">' +
            '<label class="m-r5">{{indigoLabel}}</label>' +
            // '<div class="col-xs-10">' +
            '<div class="btn-group">' +
            '<label class="btn btn-info" ng-model-options="{ debounce: 150 }" ng-model="indigoModel" ng-click="indigoModel = indigoFirst" ng-class="{active: indigoModel === indigoFirst}" ng-disabled="indigoReadonly">{{indigoFirst}}</label>' +
            '<label class="btn btn-info" ng-model-options="{ debounce: 150 }" ng-model="indigoModel" ng-click="indigoModel = indigoSecond" ng-class="{active: indigoModel === indigoSecond}" ng-disabled="indigoReadonly">{{indigoSecond}}</label>' +
            // '</div>' +
            '</div> ' +
            '</div> '
        };
    }

    /* @ngInject */
    function indigoTwoToggleCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        formUtils.doVertical(tAttrs, tElement);
        if (tAttrs.indigoLabelVertical) {
            $('<br/>').insertAfter(tElement.find('label').first());
        }

        var active = 'active';
        return active;
    }

    /* @ngInject */
    function indigoSelect(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoItems: '=',
                indigoDictionary: '@',
                indigoMultiple: '=',
                indigoLabelVertical: '=',
                indigoLabelColumnsNum: '=',
                indigoControl: '=',
                indigoPlaceHolder: '@',
                indigoItemProp: '@',
                indigoOrderByProp: '@',
                indigoClasses: '@',
                indigoChange: '&',
                indigoRemove: '&',
                indigoReadonly: '='
            },
            controller: indigoSelectController,
            compile: angular.bind({formUtils: formUtils}, indigoSelectCompile),
            template: function (tElement, tAttrs) {
                var itemProp = tAttrs.indigoItemProp || 'name';
                var content = _.chain(itemProp.split(','))
                    .map(function (prop) {
                        return '{{ $select.selected.' + prop + '}}';
                    })
                    .reduce(function (memo, num) {
                        return memo + (memo.length > 0 ? ' - ' : '') + num;
                    }, '').value();
                return '<div class="form-group {{indigoClasses}}">' +
                    '<label>{{indigoLabel}}</label>' +
                    // '<div class="col-xs-10">' +
                    '<ui-select ng-model="ctrl.selected" theme="bootstrap" ng-disabled="indigoReadonly" on-select="indigoChange()" on-remove="indigoRemove()" append-to-body="true">' +
                    '<ui-select-match placeholder="{{indigoPlaceHolder}}" >' + content + '</ui-select-match>' +
                    '<ui-select-choices repeat="item in indigoItems | filter: $select.search">' +
                    '</ui-select-choices>' +
                    '</ui-select>' +
                    // '</div>' +
                    '</div>';
            }
        };
    }

    /* @ngInject */
    function indigoSelectCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        tAttrs.indigoItemProp = tAttrs.indigoItemProp || 'name';
        tAttrs.indigoOrderByProp = tAttrs.indigoOrderByProp || 'rank';
        if (tAttrs.indigoMultiple) {
            tElement.find('ui-select').attr('multiple', true);
            tElement.find('ui-select-match').html('{{$item.' + tAttrs.indigoItemProp + '}}');
        }
        formUtils.doVertical(tAttrs, tElement);
        var select = tElement.find('ui-select-choices');
        var htmlContent = _.reduce(tAttrs.indigoItemProp.split(','), function (memo, num) {
            return memo + (memo.length > 0 ? " + ' - ' + " : '') + 'item.' + num;
        }, '');
        select.append('<span ng-bind-html="' + htmlContent + ' | highlight: $select.search"></span>');
        var repeat = select.attr('repeat');
        select.attr('repeat', repeat + ' | orderBy:"' + tAttrs.indigoOrderByProp + '"');
        formUtils.clearLabel(tAttrs, tElement);
        formUtils.setLabelColumns(tAttrs, tElement);
        return {
            post: function (scope) {
                formUtils.addOnChange(scope);
            }
        };
    }

    /* @ngInject */
    function indigoSelectController($scope, Dictionary) {
        $scope.ctrl = {selected: $scope.indigoModel};

        $scope.control = $scope.indigoControl || {};

        $scope.control.setSelection = function (select) {
            $scope.ctrl.selected = select;
        };

        $scope.control.unSelect = function () {
            $scope.ctrl.selected = {};
        };

        $scope.$watchCollection('ctrl.selected', function (newSelected) {
            $scope.indigoModel = newSelected;
        });
        $scope.$watchCollection('indigoModel', function (indigoModel) {
            $scope.ctrl.selected = indigoModel;
        });
        if ($scope.indigoDictionary) {
            Dictionary.getByName({name: $scope.indigoDictionary}, function (dictionary) {
                $scope.indigoItems = dictionary.words;
            });
        }
    }

    /* @ngInject */
    function indigoCheckbox(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoName: '@',
                indigoClasses: '@',
                indigoDisabled: '=',
                indigoChange: '&',
                indigoClick: '&',
                indigoTooltip: '@',
                indigoTooltipPlacement: '@',
                indigoNoDirty: '='
            },
            compile: angular.bind({formUtils: formUtils}, indigoCheckboxCompile),
            template: '<div class="my-checkbox-wrapper form-group {{indigoClasses}}">' +
            '<div class="checkbox">' +
            '<div class="checkbox-label-wrapper" uib-tooltip="{{indigoTooltip}}" tooltip-placement="{{indigoTooltipPlacement}}" >' +
            '<checkbox id="{{indigoName}}" class="btn-info my-checkbox" no-dirty="{{indigoNoDirty}}" ></checkbox> ' +
            '<label for="{{indigoName}}" ng-click="!indigoDisabled ? indigoModel = !indigoModel : return; indigoChangeAsync();">{{indigoLabel}}</label>' +
            '</div>' +
            '</div> ' +
            '</div> '
        };
    }


    /* @ngInject */
    function indigoCheckboxCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        formUtils.clearLabel(tAttrs, tElement);
        var $checkbox = tElement.find('checkbox');
        formUtils.addDirectivesByAttrs(tAttrs, $checkbox);
        if (tAttrs.indigoModel) {
            $checkbox.removeAttr('ng-model-options');
        }
        return {
            post: function (scope) {
                formUtils.addOnChange(scope);
            }
        };
    }

    function noDirty() {
        return {
            require: 'ngModel',
            link: noDirtyLink
        };
    }

    /* @ngInject */
    function noDirtyLink(scope, element, attrs, ngModelCtrl) {
        // override the $setDirty method on ngModelController
        if (scope.noDirty)
            ngModelCtrl.$setDirty = angular.noop;
    }

    function indigoParsersFormatters() {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                indigoParsersFormatters: '='
            },
            link: indigoParsersFormattersLink
        };
    }

    /* @ngInject */
    function indigoParsersFormattersLink(scope, element, attrs, ngModel) {
        //model -> view
        _.each(scope.indigoParsersFormatters.indigoFormatters, function (i) {
            ngModel.$formatters.push(i);
        });
        //view -> model
        _.each(scope.indigoParsersFormatters.indigoParsers, function (i) {
            ngModel.$parsers.push(i);
        });
    }

    /* @ngInject */
    function indigoInput(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            require: '?^form',
            scope: {
                indigoLabel: '@',
                indigoLabelVertical: '=',
                indigoLabelColumnsNum: '=',
                indigoName: '@',
                indigoModel: '=',
                indigoReadonly: '=',
                indigoType: '@',
                indigoInputGroup: '@',
                indigoInputSize: '@',
                indigoChange: '&',
                indigoClick: '&',
                indigoValidationRequired: '=',
                indigoValidationMaxlength: '@',
                indigoValidationMinlength: '@',
                indigoValidationPattern: '@',
                indigoValidationPatternText: '@',
                indigoClasses: '@',
                indigoParsers: '=',
                indigoFormatters: '=',
                indigoTooltip: '='
            },
            compile: angular.bind({formUtils: formUtils}, indigoInputCompile),
            template: '<div class="form-group {{indigoClasses}}">' +
            '<label>{{indigoLabel}}</label>' +
            // '<div class="col-xs-10">' +
            '<input class="form-control"/>' +
            '<div ng-show="ngModelCtrl.$invalid">' +
            '<p class="help-block" ng-if="ngModelCtrl.$error.required"> This field is required. </p>' +
            '<p class="help-block" ng-if="ngModelCtrl.$error.maxlength" > This field can\'t be longer than {{indigoValidationMaxlength}} characters.</p>' +
            '<p class="help-block" ng-if="ngModelCtrl.$error.pattern" >{{indigoValidationPatternText}}</p>' +
            // '</div>' +
            '</div>' +
            '</div>'
        };
    }

    /* @ngInject */
    function indigoInputCompile(tElement, tAttrs) {
        var formUtils = this.formUtils;

        formUtils.doVertical(tAttrs, tElement);
        var $input = tElement.find('input');
        if (tAttrs.indigoInputGroup) {
            var elementIg = $('<div class="input-group"/>');
            if (tAttrs.indigoInputSize) {
                elementIg.addClass('input-group-' + tAttrs.indigoInputSize);
            }
            var inputGroup = $input.wrap(elementIg).parent();
            if (tAttrs.indigoInputGroup === 'append') {
                inputGroup.append('<div class="input-group-btn" ng-transclude/>');
            } else if (tAttrs.indigoInputGroup === 'prepend') {
                inputGroup.prepend('<div class="input-group-btn" ng-transclude/>');
            }
        }
        formUtils.clearLabel(tAttrs, tElement);
        formUtils.setLabelColumns(tAttrs, tElement);
        formUtils.addDirectivesByAttrs(tAttrs, $input);
        $input.attr('title', '{{indigoModel}}');
        return {
            post: function (scope, iElement, iAttrs, formCtrl) {
                formUtils.showValidation(iElement, scope, formCtrl);
                formUtils.addOnChange(scope);
            }
        };
    }
})();
