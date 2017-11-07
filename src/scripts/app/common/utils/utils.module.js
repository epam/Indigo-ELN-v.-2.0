var commonHelper = require('./component-helper/component-helper');
var experimentUtil = require('./experiment-util/experiment-util.service');
var registrationMsg = require('./registration-util/registration-msg.constant');
var registrationUtil = require('./registration-util/registration-util.service');
var parseLinksService = require('./parse-links.service');
var searchUtilService = require('./search-util.service');
var tabKeyUtils = require('./tab-key.service');
var unitsConverter = require('./units-converter');

module.exports = angular
    .module('indigoeln.common.utils', [])

    .factory('commonHelper', commonHelper)
    .factory('experimentUtil', experimentUtil)
    .factory('registrationUtil', registrationUtil)
    .factory('parseLinksService', parseLinksService)
    .factory('searchUtilService', searchUtilService)
    .factory('tabKeyUtils', tabKeyUtils)
    .factory('unitsConverter', unitsConverter)

    .constant('registrationMsg', registrationMsg)

    .name;
