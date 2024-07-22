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

var entityHelper = require('./entity-helper/entity-helper.service');
var modalHelper = require('./entity-helper/modal-helper.service');
var experimentUtil = require('./experiment-util/experiment-util.service');
var registrationMsg = require('./registration-util/registration-msg.constant');
var registrationUtil = require('./registration-util/registration-util.service');
var parseLinks = require('./parse-links.service');
var searchUtil = require('./search-util.service');
var tabKeyService = require('./tab-key.service');
var unitsConverter = require('./unit-converter/units-converter.service');

module.exports = angular
    .module('indigoeln.common.utils', [])

    .factory('entityHelper', entityHelper)
    .factory('modalHelper', modalHelper)
    .factory('experimentUtil', experimentUtil)
    .factory('registrationUtil', registrationUtil)
    .factory('parseLinks', parseLinks)
    .factory('searchUtil', searchUtil)
    .factory('tabKeyService', tabKeyService)
    .factory('unitsConverter', unitsConverter)

    .constant('registrationMsg', registrationMsg)

    .name;
