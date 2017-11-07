var alertModalController = require('./alert-modal.controller');
var alertModal = require('./alert-modal.service');

module.exports = angular
    .module('indigoeln.alertModal', [])

    .controller('AlertModalController', alertModalController)

    .factory('alertModal', alertModal)

    .name;
