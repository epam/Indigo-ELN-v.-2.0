var capitalize = require('./capitalize.filter');
var containValue = require('./contain-value.filter');
var joinBy = require('./join-by.filter');
var round = require('./round.filter');
var prettyBytes = require('./pretty-bytes');
var filtersConfig = require('./filters.config');

module.exports = angular
    .module('indigoeln.common.filters', [])

    .filter('capitalize', capitalize)
    .filter('containValue', containValue)
    .filter('joinBy', joinBy)
    .filter('round', round)
    .filter('prettyBytes', prettyBytes)

    .config(filtersConfig)

    .name;
