var autorecovery = require('./autorecovery.directive');
var autorecoveryCache = require('./autorecovery.service');
var autorecoveryHelper = require('./autorecovery-helper.service');

module.exports = angular
    .module('indigoeln.autorecovery', [])

    .directive('autorecovery', autorecovery)

    .factory('autorecoveryCache', autorecoveryCache)
    .factory('autorecoveryHelper', autorecoveryHelper)

    .name;
