'use strict';

angular.module('indigoeln')
    .factory('editorUtils', function () {
        return {
            initializeEditor: function (editor, structure) {
                if (structure) {
                    editor.setMolecule(structure);
                }
            },
            getEditor: function (frame) {
                // TODO update if new editor added
                // find ketcher and define as editor
                var editor = null;
                if ('contentDocument' in frame) {
                    editor = frame.contentWindow.ketcher;
                } else {
                    // IE7
                    editor = document.frames['ifKetcher'].window.ketcher;
                }
                return editor;
            },
            getMolfile: function (editor) {
                // TODO update if new editor added
                return editor.getMolfile();
            },
        }
    })
    .directive('myEditor', function (editorUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                myStructure: '=',
                myEditorname: '@'
            },
            templateUrl: 'scripts/components/entities/template/components/structureScheme/structure-editor-template.html',
            controller: "EditorController",
            link: function (scope, element, attrs, controller) {

                var frame = element[0];
                var edt = null;
                frame.onload = function () {
                    edt = editorUtils.getEditor(frame);
                    // initialize editor
                    if (scope.myStructure.molfile) {
                        edt.setMolecule(scope.myStructure.molfile);

                    }
                };

                // derive structure's mol representation when cursor leaves the directive area
                element.on('mouseleave', function () {
                    if (edt !== null) {
                        scope.myStructure.molfile = edt.getMolfile();
                    }
                });


            }
        }
    })
    .controller("EditorController", function ($scope) {

        $scope.editors = {
            "KETCHER": {
                id: "ifKetcher",
                src: "ketcher/ketcher.html"
            }
        };

    });