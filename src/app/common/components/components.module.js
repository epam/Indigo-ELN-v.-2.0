var alertModalService = require('./alert-modal/alert-modal.module');
var autorecovery = require('./autorecovery/autorecovery.module');
var fileUploader = require('./file-uploader/file-uploader.module');
var printModal = require('./print-modal/print-modal.module');
var indigoComponents = require('./indigo-components/indigo-components.module');
var entities = require('./entities/entities.module');

var indigoTextEditor = require('./text-editor/indigo-text-editor.directive');
var CountdownDialogController = require('./timer/timer-dialog.controller');

var dependencies = [
    alertModalService,
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
