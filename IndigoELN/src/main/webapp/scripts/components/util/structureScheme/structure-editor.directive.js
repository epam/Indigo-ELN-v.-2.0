'use strict';

angular.module('indigoeln')
    .factory('editorUtils', function () {
        return {
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
                myEditorName: '@'
            },
            templateUrl: 'scripts/components/util/structureScheme/structure-editor-template.html',
            link: function (scope, element) {

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
            },
            controller: function ($scope) {
                $scope.editors = {
                    "KETCHER": {
                        id: "ifKetcher",
                        src: "vendors/ketcher/ketcher.html"
                    }
                };
            }
        }
    });