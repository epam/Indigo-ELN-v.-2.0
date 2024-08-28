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

var formUtils = require('./form-utils.service');

var indigoCheckbox = require('./elements/indigo-checkbox/indigo-checkbox.directive');
var indigoChecklist = require('./elements/indigo-checklist/indigo-checklist.directive');
var indigoSelect = require('./elements/indigo-select/indigo-select.directive');
var indigoSelectRequired = require('./elements/indigo-select/indigo-select-required.directive');
var indigoSimpleText = require('./elements/simple-text/indigo-simple-text.directive');
var indigoTextArea = require('./elements/text-area/indigo-text-area.directive');
var indigoTwoToggle = require('./elements/two-toggle/indigo-two-toggle.directive');
var indigoDatePicker = require('./elements/date-picker/indigo-date-picker.directive');

var dependencies = [];

module.exports = angular
    .module('indigoeln.indigoFormElements', dependencies)

    .factory('formUtils', formUtils)

    .directive('indigoCheckbox', indigoCheckbox)
    .directive('indigoChecklist', indigoChecklist)
    .directive('indigoSelect', indigoSelect)
    .directive('indigoSelectRequired', indigoSelectRequired)
    .directive('indigoSimpleText', indigoSimpleText)
    .directive('indigoTextArea', indigoTextArea)
    .directive('indigoTwoToggle', indigoTwoToggle)
    .directive('indigoDatePicker', indigoDatePicker)

    .name;
