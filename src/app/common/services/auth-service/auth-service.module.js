var accountService = require('./resources/account.service');
var accountRole = require('./resources/account-role.service');
var authServerProvider = require('./resources/auth.session.service');
var authExpiredInterceptor = require('./services/auth.interceptor');
var authService = require('./services/auth.service');
var principalService = require('./services/principal.service');

module.exports = angular
    .module('indigoeln.common.services.authService', [])

    .factory('accountService', accountService)
    .factory('accountRole', accountRole)
    .factory('authServerProvider', authServerProvider)
    .factory('authExpiredInterceptor', authExpiredInterceptor)
    .factory('authService', authService)
    .factory('principalService', principalService)

    .name;
