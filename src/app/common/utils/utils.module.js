var commonHelperService = require('./component-helper/component-helper.service');
var entityHelperService = require('./entity-helper/entity-helper.service');
var modalHelperService = require('./entity-helper/modal-helper.service');
var experimentUtilService = require('./experiment-util/experiment-util.service');
var registrationMsg = require('./registration-util/registration-msg.constant');
var registrationUtilService = require('./registration-util/registration-util.service');
var parseLinksService = require('./parse-links.service');
var searchUtilService = require('./search-util.service');
var tabKeyService = require('./tab-key.service');
var unitsConverterService = require('./units-converter.service');

module.exports = angular
    .module('indigoeln.common.utils', [])

    .factory('commonHelperService', commonHelperService)
    .factory('entityHelperService', entityHelperService)
    .factory('modalHelperService', modalHelperService)
    .factory('experimentUtilService', experimentUtilService)
    .factory('registrationUtilService', registrationUtilService)
    .factory('parseLinksService', parseLinksService)
    .factory('searchUtilService', searchUtilService)
    .factory('tabKeyService', tabKeyService)
    .factory('unitsConverterService', unitsConverterService)

    .constant('registrationMsg', registrationMsg)

    .name;
