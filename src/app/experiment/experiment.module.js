var experimentConfig = require('./experiment.config');
var ExperimentController = require('./component/experiment.controller');
var ExperimentCompleteModalController = require('./complete-modal/experiment-complete-modal.controller');
var CreateNewExperimentModalController =
    require('./create-new-experiment-modal/create-new-experiment-modal.controller');
var ExperimentDeleteController = require('./delete-dialog/experiment-delete-dialog.controller');
var experimentDetail = require('./experiment-detail/experiment-detail.directive');
var ExperimentSelectSignatureTemplateModalController =
    require('./select-signature-template-modal/experiment-select-signature-template-modal.controller');

var constants = require('../common/constants/constants.module');
var permissions = require('../permissions/permissions.module');

var dependencies = [
    constants,
    permissions
];

module.exports = angular
    .module('indigoeln.experiment', dependencies)

    .controller('ExperimentController', ExperimentController)
    .controller('ExperimentCompleteModalController', ExperimentCompleteModalController)
    .controller('CreateNewExperimentModalController', CreateNewExperimentModalController)
    .controller('ExperimentDeleteController', ExperimentDeleteController)
    .controller('ExperimentSelectSignatureTemplateModalController', ExperimentSelectSignatureTemplateModalController)

    .directive('experimentDetail', experimentDetail)

    .config(experimentConfig)

    .name;
