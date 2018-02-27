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

var template = require('./structure-editor-template.html');

indigoEditor.$inject = ['structureEditorService', 'authService', '$timeout'];

function indigoEditor(structureEditorService, authService, $timeout) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            indigoStructure: '=',
            indigoEditorName: '@'
        },
        bindToController: true,
        controllerAs: 'vm',
        template: template,
        link: function($scope, $element, $attrs, controller) {
            var vm = controller;
            var frame = $element[0];
            var editorInstance = null;

            // derive structure's mol representation when cursor leaves the directive area
            $element.on('mouseleave', function() {
                if (!_.isUndefined(editorInstance)) {
                    vm.indigoStructure.molfile = editorInstance.getMolfile();
                }
            });

            frame.onload = function() {
                editorInstance = structureEditorService.getEditor(frame);
                // initialize editor
                if (vm.indigoStructure.molfile) {
                    editorInstance.setMolecule(vm.indigoStructure.molfile);
                }

                $timeout(function() {
                    $element.contents().find('svg').on('click', function() {
                        authService.prolong();
                    });
                }, 1000);
            };
        },
        controller: IndigoEditorController
    };
}


function IndigoEditorController() {
    var vm = this;
    vm.editors = {
        KETCHER: {
            id: 'ifKetcher',
            src: 'vendors/ketcher/ketcher.html'
        }
    };
}

module.exports = indigoEditor;
