(function() {
    angular
        .module('indigoeln')
        .directive('indigoTextEditor', indigoTextEditor);

    /* @ngInject */
    function indigoTextEditor(textEditorConfig, $timeout) {
        return {
            scope: {
                indigoName: '@',
                indigoModel: '=',
                indigoReadonly: '=',
                onChanged: '&'
            },
            restrict: 'E',
            templateUrl: 'scripts/components/text-editor/indigo-text-editor.html',
            replace: true,
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true,
            link: link
        };

        /* @ngInject */
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
                            $scope.onChanged({text: vm.indigoModel});
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
})();
