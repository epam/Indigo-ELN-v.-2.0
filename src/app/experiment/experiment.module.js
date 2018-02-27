/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var experimentConfig = require('./experiment.config');
var ExperimentController = require('./component/experiment.controller');
var ExperimentCompleteModalController = require('./complete-modal/experiment-complete-modal.controller');
var CreateNewExperimentModalController =
    require('./create-new-experiment-modal/create-new-experiment-modal.controller');
var ExperimentDeleteDialogController = require('./delete-dialog/experiment-delete-dialog.controller');
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
    .controller('ExperimentDeleteDialogController', ExperimentDeleteDialogController)
    .controller('ExperimentSelectSignatureTemplateModalController', ExperimentSelectSignatureTemplateModalController)

    .directive('experimentDetail', experimentDetail)

    .config(experimentConfig)

    .name;
