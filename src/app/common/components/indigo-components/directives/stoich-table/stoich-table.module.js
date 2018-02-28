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

var indigoStoichTable = require('./directives/stoich-table/indigo-stoich-table.directive');

var stoichColumnActions = require('./services/stoich-column-actions.service');
var stoichProductColumns = require('./services/stoich-product-columns.service');
var stoichReactantsColumns = require('./services/stoich-reactants-columns.service');
var stoichTableHelper = require('./services/stoich-table-helper');

var reagentsCalculation = require('./services/reagents/reagents-calculation.service');
var productsCalculation = require('./services/products/products-calculation.service');

var stoichTableCache = require('./services/stoich-table-cache.service');

var dependencies = [];

module.exports = angular
    .module('indigoeln.stoichTable', dependencies)

    .directive('indigoStoichTable', indigoStoichTable)

    .factory('stoichColumnActions', stoichColumnActions)
    .factory('stoichProductColumns', stoichProductColumns)
    .factory('stoichReactantsColumns', stoichReactantsColumns)
    .factory('stoichTableCache', stoichTableCache)
    .factory('stoichTableHelper', stoichTableHelper)
    .factory('reagentsCalculation', reagentsCalculation)
    .factory('productsCalculation', productsCalculation)

    .name;
