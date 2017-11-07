var indigoStoichTable = require('./indigo-stoich-table.directive');

var stoichColumnActions = require('./stoich-column-actions.factory');
var stoichProductColumns = require('./stoich-product-columns.factory');
var stoichReactantsColumns = require('./stoich-reactants-columns.factory');

var stoichTableCache = require('./stoich-table-cache.service');

var dependencies = [];

module.export = angular
    .module('indigoeln.stoichTable', dependencies)

    .directive('indigoStoichTable', indigoStoichTable)

    .factory('stoichColumnActions', stoichColumnActions)
    .factory('stoichProductColumns', stoichProductColumns)
    .factory('stoichReactantsColumns', stoichReactantsColumns)
    .factory('stoichTableCache', stoichTableCache)

    .name;
