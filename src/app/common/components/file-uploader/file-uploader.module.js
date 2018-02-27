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

var FileUploaderDeleteDialogController = require('./delete-dialog/delete-dialog.controller');
var FileUploaderController = require('./file-uploader/file-uploader.controller');
var MultipleFileUploaderController = require('./multiple-file-uploader/multiple-file-uploader.controller');
var SingleFileUploaderController = require('./single-file-uploader/single-file-uploader.controller');

var indigoFileUploader = require('./file-uploader/indigo-file-uploader.directive');
var experimentFileUploaderService = require('./resources/experiment-file-uploader.service');
var projectFileUploaderService = require('./resources/project-file-uploader.service');
var fileUploader = require('./services/file-uploader.service');

var dependencies = [];

module.exports = angular
    .module('indigoeln.fileUploader', dependencies)

    .controller('FileUploaderDeleteDialogController', FileUploaderDeleteDialogController)
    .controller('FileUploaderController', FileUploaderController)
    .controller('MultipleFileUploaderController', MultipleFileUploaderController)
    .controller('SingleFileUploaderController', SingleFileUploaderController)

    .directive('indigoFileUploader', indigoFileUploader)

    .factory('experimentFileUploaderService', experimentFileUploaderService)
    .factory('projectFileUploaderService', projectFileUploaderService)
    .factory('fileUploader', fileUploader)

    .name;
