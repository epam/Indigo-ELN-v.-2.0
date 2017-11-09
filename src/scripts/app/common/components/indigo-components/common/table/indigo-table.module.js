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

var inputService = require('./input/set-input.service');
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

    .factory('inputService', inputService)
    .factory('scalarService', scalarService)
    .factory('selectService', selectService)
    .factory('unitService', unitService)

    .filter('unit', unit)

    .run(run)

    .name;
