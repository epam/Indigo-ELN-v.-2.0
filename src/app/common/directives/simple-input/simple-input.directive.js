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

require('./simple-input.less');
var template = require('./simple-input.html');

/* @ngInject */
function simpleInput($compile, $log) {
    return {
        restrict: 'E',
        transclude: true,
        require: '?^form',
        scope: {
            validationPatternText: '@?'
        },
        link: function($scope, $element, $attr, formCtrl) {
            $element.addClass('form-group');
            var $input = $element.find('input');

            if ($input[0].attributes['ng-required'] || $input[0].attributes.required) {
                if (!formCtrl) {
                    $log.error('Element doesn\'t have parent form, but it is required', $element);

                    return;
                }

                $element.addClass('required');
                $scope.ngModelCtrl = formCtrl[$input.attr('name')];
                var el = $compile(template)($scope);
                $input.after(el);
            }
        },
        template: '<label ng-transclude></label>'
    };
}

module.exports = simpleInput;
