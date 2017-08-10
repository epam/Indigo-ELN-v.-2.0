(function() {
    angular
        .module('indigoeln')
        .directive('indigoTextEditor', indigoTextEditor);

    /* @ngInject */
    function indigoTextEditor(textEditorConfig) {
        return {
            scope: {
                indigoName: '@',
                indigoModel: '=',
                indigoReadonly: '=',
                onChanged: '&'
            },
            restrict: 'E',
            template: '<textarea name={{::indigoName}} class="form-control" ng-model="indigoModel"' +
            ' data-autosave="editor-content" autofocus></textarea>',
            replace: true,
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

                if ($scope.indigoReadonly === true) {
                    editor.body.attr('contenteditable', false);
                }

                bindEvents();
            }

            function bindEvents() {
                $scope.$watch('indigoModel', function(value) {
                    if (value !== editor.getValue()) {
                        editor.setValue(value);
                    }
                });

                var editorListener = editor.on('valuechanged', function() {
                    if (($scope.indigoModel || '') !== editor.getValue() && editor.getValue() !== '<p>undefined</p>') {
                        $scope.indigoModel = editor.getValue();
                        $scope.onChanged({text: $scope.indigoModel});
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
