var alertModal = require('./alert-modal/alert-modal.module');
var autorecovery = require('./autorecovery/autorecovery.module');
var fileUploader = require('./file-uploader/file-uploader');
var printModal = require('./print-modal/print-modal.module');
var indigoTextEditor = require('./text-editor/indigo-text-editor.directive');
var componentsModule = require('./indigo-components/components.module');

var dependecies = [
    autorecovery,
    fileUploader,
    printModal,
    componentsModule,
    alertModal
];

module.export = angular.module('indigoeln.commonModule.componentsModule', dependecies)
    .directive('indigoTextEditor', indigoTextEditor)

    .name;
