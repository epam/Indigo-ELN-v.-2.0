var constants = require('./constants/constants.module');
var filters = require('./filters/filters.module');
var services = require('./services/services.module');
var i18n = require('./i18n/i18n.module');
var interceptors = require('./interceptors/interceptors.module');
var utils = require('./utils/utils.module');
var commonDirectives = require('./directives/directives.module');

var commonConfig = require('./common.config');

var dependencies = [
    constants,
    filters,
    services,
    i18n,
    interceptors,
    utils,
    commonDirectives
];

module.exports = angular
    .module('indigoeln.common', dependencies)

    .config('commonConfig', commonConfig)

    .name;
