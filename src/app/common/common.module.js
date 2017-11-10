var components = require('./components/components.module');
var constants = require('./constants/constants.module');
var directives = require('./directives/directives.module');
var filters = require('./filters/filters.module');
var i18n = require('./i18n/i18n.module');
var interceptors = require('./interceptors/interceptors.module');
var resources = require('./resources/resources.module');
var services = require('./services/services.module');
var utils = require('./utils/utils.module');

var commonConfig = require('./common.config');

var dependencies = [
    components,
    constants,
    directives,
    filters,
    i18n,
    interceptors,
    resources,
    services,
    utils
];

module.exports = angular
    .module('indigoeln.common', dependencies)

    .config(commonConfig)

    .name;
