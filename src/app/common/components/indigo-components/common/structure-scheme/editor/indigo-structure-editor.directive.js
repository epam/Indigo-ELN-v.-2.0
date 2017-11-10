var template = require('./structure-editor-template.html');

indigoEditor.$inject = ['editorUtils', 'authService', '$timeout'];

function indigoEditor(editorUtils, authService, $timeout) {
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
                editorInstance = editorUtils.getEditor(frame);
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
