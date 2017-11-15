var indigoStoichTable = require('./indigo-stoich-table.directive');

var stoichColumnActionsService = require('./stoich-column-actions.service');
var stoichProductColumnsService = require('./stoich-product-columns.service');
var stoichReactantsColumnsService = require('./stoich-reactants-columns.service');

var stoichTableCacheService = require('./stoich-table-cache.service');

var dependencies = [];

module.exports = angular
    .module('indigoeln.stoichTable', dependencies)

    .directive('indigoStoichTable', indigoStoichTable)

    .factory('stoichColumnActionsService', stoichColumnActionsService)
    .factory('stoichProductColumnsService', stoichProductColumnsService)
    .factory('stoichReactantsColumnsService', stoichReactantsColumnsService)
    .factory('stoichTableCacheService', stoichTableCacheService)

    .name;
