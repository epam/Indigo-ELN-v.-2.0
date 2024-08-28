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

var capitalize = require('./capitalize.filter');
var containValue = require('./contain-value.filter');
var joinBy = require('./join-by.filter');
var round = require('./round.filter');
var prettyBytes = require('./pretty-bytes.filter');
var customNumber = require('./custom-number.filter');
var filtersConfig = require('./filters.config');

module.exports = angular
    .module('indigoeln.common.filters', [])

    .filter('capitalize', capitalize)
    .filter('containValue', containValue)
    .filter('joinBy', joinBy)
    .filter('round', round)
    .filter('prettyBytes', prettyBytes)
    .filter('customNumber', customNumber)

    .config(filtersConfig)

    .name;
