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

require('./indigo-text-editor.less');
var Simditor = require('../../../dependencies/simditor');
var template = require('./indigo-text-editor.html');
var textEditorConfig = require('./text-editor.json');

indigoTextEditor.$inject = ['$timeout'];

function indigoTextEditor($timeout) {
    return {
        scope: {
            indigoName: '@',
            indigoModel: '=',
            indigoReadonly: '=',
            onChanged: '&'
        },
        restrict: 'E',
        template: template,
        controller: angular.noop,
        controllerAs: 'vm',
        bindToController: true,
        link: link
    };

    function link($scope, $element, $attr, vm) {
        var editor;

        init();

        function init() {
            Simditor.locale = 'en_EN';

            vm.startEdit = startEdit;
        }

        function startEdit() {
            if (vm.indigoReadonly) {
                return;
            }

            vm.isEditing = true;
            initEditor();
        }

        function initEditor() {
            if (editor) {
                return;
            }

            editor = new Simditor(
                angular.extend({
                    locale: 'en_EN',
                    textarea: $element
                }, textEditorConfig)
            );
            initListeners();
        }

        function initListeners() {
            var editorListener = editor.on('valuechanged', function() {
                if (angular.isDefined(vm.indigoModel) && vm.indigoModel !== editor.getValue()) {
                    $timeout(function() {
                        vm.indigoModel = editor.getValue();
                        vm.onChanged({text: vm.indigoModel});
                    });
                }
            });

            $scope.$watch('vm.indigoModel', function(value) {
                if (editor && value !== editor.getValue()) {
                    editor.setValue(value || '');
                }
            });

            $scope.$on('$destroy', function() {
                editorListener.off();
            });
        }
    }
}

module.exports = indigoTextEditor;
