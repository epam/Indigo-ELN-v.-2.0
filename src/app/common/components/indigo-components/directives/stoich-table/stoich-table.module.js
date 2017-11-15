var indigoStoichTable = require('./indigo-stoich-table.directive');

var stoichColumnActions = require('./stoich-column-actions.service');
var stoichProductColumns = require('./stoich-product-columns.service');
var stoichReactantsColumns = require('./stoich-reactants-columns.service');

var stoichTableCache = require('./stoich-table-cache.service');

var dependencies = [];

module.exports = angular
    .module('indigoeln.stoichTable', dependencies)

    .directive('indigoStoichTable', indigoStoichTable)

    .factory('stoichColumnActions', stoichColumnActions)
    .factory('stoichProductColumns', stoichProductColumns)
    .factory('stoichReactantsColumns', stoichReactantsColumns)
    .factory('stoichTableCache', stoichTableCache)

    .name;
