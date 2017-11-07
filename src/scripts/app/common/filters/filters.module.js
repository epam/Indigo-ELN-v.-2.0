var capitalize = require('./capitalize.filter');
var containValue = require('./contain-value.filter');
var joinBy = require('./join-by.filter');
var round = require('./round.filter');
var filtersConfig = require('./filters.config');

module.exports = angular
    .module('indigoeln.common.filters', [])

    .filter('capitalize', capitalize)
    .filter('containValue', containValue)
    .filter('joinBy', joinBy)
    .filter('round', round)

    .config('filtersConfig', filtersConfig)

    .name;
