var indigoEditor = require('./editor/indigo-structure-editor.directive');
var indigoStructureScheme = require('./scheme/indigo-structure-scheme.directive');

var structureEditorService = require('./editor/structure-editor.service');

var StructureEditorModalController = require('./structure-editor-modal/structure-editor-modal.controller');
var StructureExportModalController = require('./export/structure-export-modal.controller');
var StructureImportModalController = require('./import/structure-import-modal.controller');
var StructureSchemeController = require('./scheme/structure-scheme.controller');


module.exports = angular
    .module('indigoeln.structureScheme', [])

    .directive('indigoEditor', indigoEditor)
    .directive('indigoStructureScheme', indigoStructureScheme)

    .controller('StructureEditorModalController', StructureEditorModalController)
    .controller('StructureExportModalController', StructureExportModalController)
    .controller('StructureImportModalController', StructureImportModalController)
    .controller('StructureSchemeController', StructureSchemeController)

    .factory('structureEditorService', structureEditorService)

    .name;
