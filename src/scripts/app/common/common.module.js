var services = require('./services/services.module');
var i18n = require('./i18n/i18n.module');
var interceptors = require('./interceptors/interceptors.module');
var utils = require('./utils/utils.module');

var dependencies = [
    services,
    i18n,
    interceptors,
    utils
];

module.exports = angular
    .module('indigoeln.common', dependencies)

    .name;
