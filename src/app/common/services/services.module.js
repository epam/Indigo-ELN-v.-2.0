var appValuesService = require('./app-values/app-values.service');
var calculationService = require('./calculation-service/calculation.service');
var configService = require('./config-service/config.service');
var ConfirmationModalController = require('./confirmation-modal/confirmation-modal.controller');
var confirmationModalService = require('./confirmation-modal/confirmation-modal.service');
var notifyService = require('./notify-service/notify.service');
var scrollToService = require('./scroll-to/scroll-to.service');
var simpleLocalCacheService = require('./simple-local-cache/simple-local-cache.service');
var usersService = require('./users-service/users.service');
var wsService = require('./ws-service/ws.service');
var entitiesBrowserService = require('./entities-browser/entities-browser.service');
var entitiesCacheService = require('./entities-cache/entities-cache.service');

var authService = require('./auth-service/auth-service.module');
var sd = require('./sd/sd.module');

var dependencies = [
    authService,
    sd
];

module.exports = angular
    .module('indigoeln.common.services', dependencies)

    .controller('ConfirmationModalController', ConfirmationModalController)

    .factory('appValuesService', appValuesService)
    .factory('calculationService', calculationService)
    .factory('configService', configService)
    .factory('confirmationModalService', confirmationModalService)
    .factory('notifyService', notifyService)
    .factory('scrollToService', scrollToService)
    .factory('simpleLocalCacheService', simpleLocalCacheService)
    .factory('usersService', usersService)
    .factory('wsService', wsService)
    .factory('entitiesBrowserService', entitiesBrowserService)
    .factory('entitiesCacheService', entitiesCacheService)

    .name;
