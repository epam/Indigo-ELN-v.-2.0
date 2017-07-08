/* jshint browser: true */
(function() {
    angular
        .module('indigoeln')
        .directive('indigoEditor', indigoEditor);

    /* @ngInject */
    function indigoEditor(editorUtils, Auth, $timeout) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoStructure: '=',
                indigoEditorName: '@'
            },
            bindToController: true,
            controllerAs: 'vm',
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/editor/structure-editor-template.html',
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
                        // TODO: Why the prolongation make only for svg tag?
                        $element.contents().find('svg').on('click', function() {
                            Auth.prolong();
                        });
                    }, 1000);
                };
            },
            controller: indigoEditorController
        };
    }

    /* @ngInject */
    function indigoEditorController() {
        var vm = this;
        vm.editors = {
            KETCHER: {
                id: 'ifKetcher',
                src: 'vendors/ketcher/ketcher.html'
            }
        };
    }
})();
