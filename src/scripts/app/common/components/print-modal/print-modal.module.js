var printModal = require('./print-modal.factory');
var PrintModalController = require('./print-modal.controller');

module.export = angular
    .module('indigoeln.printModal', [])

    .controller('PrintModalController', PrintModalController)

    .factory('printModal', printModal)

    .name;
