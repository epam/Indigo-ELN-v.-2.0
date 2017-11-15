var autorecovery = require('./autorecovery.directive');
var autorecoveryCacheService = require('./autorecovery.service');
var autorecoveryHelperService = require('./autorecovery-helper.service');

module.exports = angular
    .module('indigoeln.autorecovery', [])

    .directive('autorecovery', autorecovery)

    .factory('autorecoveryCacheService', autorecoveryCacheService)
    .factory('autorecoveryHelperService', autorecoveryHelperService)

    .name;
