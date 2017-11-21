var commonHelper = require('./component-helper/component-helper.service');
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

    .factory('commonHelper', commonHelper)
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
