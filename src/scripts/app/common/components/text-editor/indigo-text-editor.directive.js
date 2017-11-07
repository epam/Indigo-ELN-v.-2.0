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
        replace: true,
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

            editor = new Simditor(
                angular.extend({
                    textarea: $element
                }, textEditorConfig)
            );

            if (vm.indigoReadonly === true) {
                editor.body.attr('contenteditable', false);
            }

            bindEvents();
        }

        function bindEvents() {
            $scope.$watch('indigoModel', function(value) {
                if (value !== editor.getValue()) {
                    editor.setValue(value || '');
                }
            });

            var editorListener = editor.on('valuechanged', function() {
                if (angular.isDefined(vm.indigoModel) && vm.indigoModel !== editor.getValue()) {
                    $timeout(function() {
                        vm.indigoModel = editor.getValue();
                        vm.onChanged({text: vm.indigoModel});
                    });
                }
            });

            $scope.$watch('indigoReadonly', function(newValue) {
                editor.body.attr('contenteditable', !newValue);
            });

            $scope.$on('$destroy', function() {
                editorListener.off();
            });
        }
    }
}

module.export = indigoTextEditor;
