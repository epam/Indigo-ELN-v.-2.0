var entityHelper = require('./entity-helper.service');
var modalHelper = require('./modal-helper.service');

module.exports = angular
    .module('indigoeln.entities.helpers', [])

    .factory('entityHelper', entityHelper)
    .factory('modalHelper', modalHelper)

    .name;
