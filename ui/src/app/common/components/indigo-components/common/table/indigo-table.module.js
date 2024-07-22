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

require('./image-popover.html');
require('./indigo-table.less');

var indigoTable = require('./indigo-table.directive');
var cellTyper = require('./cell-typer/cell-typer.directive');
var columnsSettings = require('./columns-settings/columns-settings.directive');
var editableCell = require('./editable-cell/editable-cell.directive');
var simpleCheckbox = require('./simple-checkbox/simple-checkbox.directive');

var SetInputValueController = require('./input/set-input-value.controller');
var SetSelectValueController = require('./select/set-select-value.controller');
var SetUnitValueController = require('./unit/set-unit-value.controller');

var setInputService = require('./input/set-input.service');
var scalarService = require('./scalar/scalar.service');
var selectService = require('./select/select.service');
var unitService = require('./unit/unit.service');

var unit = require('./unit/unit.filter');

var run = require('./indigo-table.run');

module.exports = angular
    .module('indigoeln.indigoTable', [])

    .directive('indigoTable', indigoTable)
    .directive('cellTyper', cellTyper)
    .directive('columnsSettings', columnsSettings)
    .directive('editableCell', editableCell)
    .directive('simpleCheckbox', simpleCheckbox)

    .controller('SetInputValueController', SetInputValueController)
    .controller('SetSelectValueController', SetSelectValueController)
    .controller('SetUnitValueController', SetUnitValueController)

    .factory('setInputService', setInputService)
    .factory('scalarService', scalarService)
    .factory('selectService', selectService)
    .factory('unitService', unitService)

    .filter('unit', unit)

    .run(run)

    .name;
