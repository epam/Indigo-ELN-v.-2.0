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
