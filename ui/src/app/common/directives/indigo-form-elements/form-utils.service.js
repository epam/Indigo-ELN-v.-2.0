/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

formUtils.$inject = ['$timeout'];

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
                        $inputs.each(function() {
                            scope.$watch(function() {
                                return ngModelCtrl.$invalid && (ngModelCtrl.$dirty || formCtrl.$submitted);
                            }, function(isInvalid) {
                                $formGroup.toggleClass('has-error', isInvalid);
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
                $timeout(scope.indigoChange, 0);
            };
        }
        if (scope.indigoClick) {
            scope.indigoClickAsync = function() {
                $timeout(scope.indigoClick, 0);
            };
        }
    }

    function addDirectiveByAttrsValues(tAttrs, $element) {
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
    }

    function addDirectiveByAttrsEvents(tAttrs, $element) {
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
    }

    function addDirectiveByAttrsFormats(tAttrs, $element) {
        if (tAttrs.indigoParsers || tAttrs.indigoFormatters) {
            $element.attr(
                'indigo-parsers-formatters',
                '{indigoParsers: indigoParsers, indigoFormatters: indigoFormatters}'
            );
        }

        if (tAttrs.indigoReadonly) {
            $element.attr('ng-readonly', 'indigoReadonly');
        }
        if (tAttrs.indigoName) {
            $element.attr('name', '{{indigoName}}');
        }
        if (!_.isUndefined(undefined)) {
            $element.attr('min', tAttrs.indigoNumberMin);
        }
        if (tAttrs.indigoType) {
            $element.attr('type', '{{indigoType}}');
            if (tAttrs.indigoType === 'number') {
                $element.attr('step', _.isUndefined(tAttrs.step) ? 'any' : tAttrs.step);
            }
        }
        if (tAttrs.indigoDisabled) {
            $element.attr('ng-disabled', 'indigoDisabled');
        }
    }

    function addDirectivesByAttrs(tAttrs, $element) {
        addDirectiveByAttrsValues(tAttrs, $element);
        addDirectiveByAttrsEvents(tAttrs, $element);
        addDirectiveByAttrsFormats(tAttrs, $element);
    }
}

module.exports = formUtils;
