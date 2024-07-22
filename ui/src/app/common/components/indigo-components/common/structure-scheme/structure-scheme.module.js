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
