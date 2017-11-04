var experimentConfig = require('./experiment.config');
var ExperimentController = require('./experiment.controller');
var ExperimentCompleteModalController = require('./complete-modal/experiment-complete-modal.controller');
var CreateNewExperimentModalController = require('./create-new-experiment-modal/create-new-experiment-modal.controller');
var ExperimentDeleteController = require('./delete-dialog/experiment-delete-dialog.controller');
var experimentDetail = require('./experiment-detail/experiment-detail.directive');
var ExperimentSelectSignatureTemplateModalController =
    require('./select-signature-template-modal/experiment-select-signature-template-modal.controller');

//TODO Add common constants
var dependencies = [];

module.exports = angular
    .module('indigoeln.entities.experiment', [
        'indigoeln.constantsModule'
    ])

    .controller('ExperimentController', ExperimentController)
    .controller('ExperimentCompleteModalController', ExperimentCompleteModalController)
    .controller('CreateNewExperimentModalController', CreateNewExperimentModalController)
    .controller('ExperimentDeleteController', ExperimentDeleteController)
    .controller('ExperimentSelectSignatureTemplateModalController', ExperimentSelectSignatureTemplateModalController)

    .directive('experimentDetail', experimentDetail)

    .config(experimentConfig)

    .name;
