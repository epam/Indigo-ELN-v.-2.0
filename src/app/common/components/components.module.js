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

var alertModal = require('./alert-modal/alert-modal.module');
var autorecovery = require('./autorecovery/autorecovery.module');
var fileUploader = require('./file-uploader/file-uploader.module');
var printModal = require('./print-modal/print-modal.module');
var indigoComponents = require('./indigo-components/indigo-components.module');
var entities = require('./entities/entities.module');

var indigoTextEditor = require('./text-editor/indigo-text-editor.directive');
var CountdownDialogController = require('./timer/timer-dialog.controller');

var dependencies = [
    alertModal,
    autorecovery,
    fileUploader,
    indigoComponents,
    printModal,
    entities
];

module.exports = angular
    .module('indigoeln.common.components', dependencies)

    .controller('CountdownDialogController', CountdownDialogController)

    .directive('indigoTextEditor', indigoTextEditor)

    .name;
