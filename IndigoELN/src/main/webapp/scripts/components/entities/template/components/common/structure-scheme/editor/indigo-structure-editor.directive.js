/* jshint browser: true */
(function () {
    angular
        .module('indigoeln')
        .directive('indigoEditor', indigoEditor);

    /* @ngInject */
    function indigoEditor(editorUtils, Auth) {
        var bindings = {
            editorUtils: editorUtils,
            Auth: Auth
        };

        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoStructure: '=',
                indigoEditorName: '@'
            },
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/editor/structure-editor-template.html',
            link: angular.bind(bindings, link),
            controller: controller
        };
    }

    function link(scope, element) {
        var editorUtils = this.editorUtils,
            Auth = this.Auth;

        var frame = element[0];
        var edt = null;
        frame.onload = function () {
            edt = editorUtils.getEditor(frame);
            // initialize editor
            if (scope.indigoStructure.molfile) {
                edt.setMolecule(scope.indigoStructure.molfile);
            }

            setTimeout(function () {
                var $frame = $(frame);
                $frame.contents().find('svg').on('click', function () {
                    Auth.prolong();
                });
            }, 1000);
        };

        // derive structure's mol representation when cursor leaves the directive area
        element.on('mouseleave', function () {
            if (edt !== null) {
                scope.indigoStructure.molfile = edt.getMolfile();
            }
        });
    }

    /* @ngInject */
    function controller($scope) {
        $scope.editors = {
            'KETCHER': {
                id: 'ifKetcher',
                src: 'vendors/ketcher/ketcher.html'
            }
        };
    }
})();