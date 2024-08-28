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

var template = require('./two-toggle.html');

indigoTwoToggle.$inject = ['formUtils'];

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
        compile: compile,
        template: template
    };

    /* @ngInject */
    function compile($element, tAttrs) {
        formUtils.doVertical(tAttrs, $element);
        if (tAttrs.indigoLabelVertical) {
            angular.element('<br/>').insertAfter($element.find('label').first());
        }

        return 'active';
    }
}

module.exports = indigoTwoToggle;
