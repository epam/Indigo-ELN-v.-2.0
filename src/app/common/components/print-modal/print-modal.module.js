var printModal = require('./print-modal.service');
var PrintModalController = require('./print-modal.controller');

module.exports = angular
    .module('indigoeln.printModal', [])

    .controller('PrintModalController', PrintModalController)

    .factory('printModal', printModal)

    .name;
