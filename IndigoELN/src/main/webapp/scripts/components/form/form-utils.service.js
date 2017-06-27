angular
    .module('indigoeln')
    .factory('formUtils', formUtils);

/* @ngInject */
function formUtils($timeout) {
    return {
        doVertical: doVertical,
        clearLabel: clearLabel,
        setLabelColumns: setLabelColumns,
        showValidation: showValidation,
        addOnChange: addOnChange,
        addDirectivesByAttrs: addDirectivesByAttrs
    };

    function doVertical(tAttrs, tElement) {
        if (tAttrs.indigoLabelVertical && tAttrs.indigoLabel) {
            tElement.find('.col-xs-2').removeClass('col-xs-2');
            tElement.find('.col-xs-10').children().unwrap();
        }
    }

    function clearLabel(tAttrs, tElement) {
        if (!tAttrs.indigoLabel) {
            tElement.find('label').remove();
            tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-12');
        }
    }

    function setLabelColumns(tAttrs, tElement) {
        if (tAttrs.indigoLabelColumnsNum) {
            var labelColumnsNum = parseInt(tAttrs.indigoLabelColumnsNum, 10);
            if (_.isNumber(labelColumnsNum) && labelColumnsNum > 0 && labelColumnsNum < 12) {
                tElement.find('label').removeClass('col-xs-2').addClass('col-xs-' + labelColumnsNum);
                tElement.find('.col-xs-10').removeClass('col-xs-10').addClass('col-xs-' + (12 - labelColumnsNum));
            }
        }
    }

    function showValidation($formGroup, scope, formCtrl) {
        $timeout(function() {
            if (formCtrl) {
                var ngModelCtrl = formCtrl[scope.indigoName];
                if (ngModelCtrl) {
                    scope.ngModelCtrl = ngModelCtrl;
                    var $inputs = $formGroup.find('input[ng-model],textarea[ng-model],select[ng-model]');
                    if ($inputs.length > 0) {
                        var unbinds = [];
                        $inputs.each(function() {
                            unbinds.push(scope.$watch(function() {
                                return ngModelCtrl.$invalid && (ngModelCtrl.$dirty || formCtrl.$submitted);
                            }, function(isInvalid) {
                                $formGroup.toggleClass('has-error', isInvalid);
                            }));
                        });
                        scope.$on('$destroy', function() {
                            _.each(unbinds, function(unbind) {
                                unbind();
                            });
                        });
                    }
                }
            }
        }, 0, false);
    }

    function addOnChange(scope) {
        if (scope.indigoChange) {
            scope.indigoChangeAsync = function() {
                $timeout(scope.indigoChange, 0, false);
            };
        }
        if (scope.indigoClick) {
            scope.indigoClickAsync = function() {
                $timeout(scope.indigoClick, 0, false);
            };
        }
    }

    function addDirectivesByAttrs(tAttrs, $element) {
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
        if (angular.isDefined(undefined)) {
            $element.attr('min', tAttrs.indigoNumberMin);
        }
        if (tAttrs.indigoType) {
            $element.attr('type', '{{indigoType}}');
        }
        if (tAttrs.indigoDisabled) {
            $element.attr('ng-disabled', 'indigoDisabled');
        }
    }
}
