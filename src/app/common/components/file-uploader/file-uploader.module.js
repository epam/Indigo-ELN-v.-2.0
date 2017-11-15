var FileUploaderDeleteDialogController = require('./delete-dialog/delete-dialog.controller');
var FileUploaderController = require('./file-uploader/file-uploader.controller');
var MultipleFileUploaderController = require('./multiple-file-uploader/multiple-file-uploader.controller');
var SingleFileUploaderController = require('./single-file-uploader/single-file-uploader.controller');

var indigoFileUploader = require('./file-uploader/indigo-file-uploader.directive');
var experimentFileUploaderService = require('./resources/experiment-file-uploader.service');
var projectFileUploaderService = require('./resources/project-file-uploader.service');
var fileUploaderService = require('./services/file-uploader.service');

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
    .factory('fileUploaderService', fileUploaderService)

    .name;
