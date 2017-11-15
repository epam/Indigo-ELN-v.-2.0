var alertModalController = require('./alert-modal.controller');
var alertModalService = require('./alert-modal.service');

module.exports = angular
    .module('indigoeln.alertModalService', [])

    .controller('AlertModalController', alertModalController)

    .factory('alertModalService', alertModalService)

    .name;
