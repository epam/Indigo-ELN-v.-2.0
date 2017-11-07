var formUtils = require('./form-utils.service');

var indigoCheckbox = require('./elements/indigo-checkbox/indigo-checkbox.directive');
var indigoChecklist = require('./elements/indigo-checklist/indigo-checklist.directive');
var indigoSelect = require('./elements/indigo-select/indigo-select.directive');
var indigoSimpleText = require('./elements/simple-text/indigo-simple-text.directive');
var indigoTextArea = require('./elements/text-area/indigo-text-area.directive');
var indigoTwoToggle = require('./elements/two-toggle/indigo-two-toggle.directive');

var dependencies = [];

module.exports = angular
    .module('indigoeln.indigoFormElements', dependencies)

    .factory('formUtils', formUtils)

    .directive('indigo-checkbox', indigoCheckbox)
    .directive('indigoChecklist', indigoChecklist)
    .directive('indigoSelect', indigoSelect)
    .directive('indigoSimpleText', indigoSimpleText)
    .directive('indigoTextArea', indigoTextArea)
    .directive('indigoTwoToggle', indigoTwoToggle)

    .name;
