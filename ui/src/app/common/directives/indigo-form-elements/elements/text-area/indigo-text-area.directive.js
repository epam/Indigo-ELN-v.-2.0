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

var template = require('./text-area.html');

indigoTextArea.$inject = ['formUtils'];

function indigoTextArea(formUtils) {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {
            indigoLabel: '@',
            indigoModel: '=',
            indigoNoElastic: '=',
            indigoInputGroup: '@',
            indigoReadonly: '=',
            indigoTooltip: '=',
            indigoTrim: '='
        },
        compile: compile,
        template: template
    };

    /* @ngInject */
    function compile(tElement, tAttrs) {
        formUtils.addDirectivesByAttrs(tAttrs, tElement.find('textarea'));

        if (tAttrs.indigoTrim) {
            tElement.find('textarea').attr('ng-trim', tAttrs.indigoTrim);
        }

        if (tAttrs.indigoNoElastic && tAttrs.indigoNoElastic === 'true') {
            tElement.find('textarea').removeAttr('msd-elastic');
        }
    }
}

module.exports = indigoTextArea;
