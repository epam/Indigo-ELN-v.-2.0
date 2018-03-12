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

var appValuesService = require('./app-values/app-values.service');
var calculationService = require('./calculation-service/calculation.service');
var configService = require('./config-service/config.service');
var ConfirmationModalController = require('./confirmation-modal/confirmation-modal.controller');
var confirmationModal = require('./confirmation-modal/confirmation-modal.service');
var loadingModalService = require('./loading-modal/loading-modal.service');
var notifyService = require('./notify-service/notify.service');
var scrollToService = require('./scroll-to/scroll-to.service');
var simpleLocalCache = require('./simple-local-cache/simple-local-cache.service');
var usersService = require('./users-service/users.service');
var wsService = require('./ws-service/ws.service');
var entitiesBrowserService = require('./entities-browser/entities-browser.service');
var entitiesCache = require('./entities-cache/entities-cache.service');

var authService = require('./auth-service/auth-service.module');
var sd = require('./sd/sd.module');

var dependencies = [
    authService,
    sd
];

module.exports = angular
    .module('indigoeln.common.services', dependencies)

    .controller('ConfirmationModalController', ConfirmationModalController)

    .factory('loadingModalService', loadingModalService)
    .factory('appValuesService', appValuesService)
    .factory('calculationService', calculationService)
    .factory('configService', configService)
    .factory('confirmationModal', confirmationModal)
    .factory('notifyService', notifyService)
    .factory('scrollToService', scrollToService)
    .factory('simpleLocalCache', simpleLocalCache)
    .factory('usersService', usersService)
    .factory('wsService', wsService)
    .factory('entitiesBrowserService', entitiesBrowserService)
    .factory('entitiesCache', entitiesCache)

    .name;
