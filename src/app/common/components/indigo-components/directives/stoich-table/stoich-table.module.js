var indigoStoichTable = require('./directives/stoich-table/indigo-stoich-table.directive');

var stoichColumnActions = require('./services/stoich-column-actions.service');
var stoichProductColumns = require('./services/stoich-product-columns.service');
var stoichReactantsColumns = require('./services/stoich-reactants-columns.service');

var stoichTableCache = require('./services/stoich-table-cache.service');

var dependencies = [];

module.exports = angular
    .module('indigoeln.stoichTable', dependencies)

    .directive('indigoStoichTable', indigoStoichTable)

    .factory('stoichColumnActions', stoichColumnActions)
    .factory('stoichProductColumns', stoichProductColumns)
    .factory('stoichReactantsColumns', stoichReactantsColumns)
    .factory('stoichTableCache', stoichTableCache)

    .name;
