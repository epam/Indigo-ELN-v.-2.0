angular.module('indigoeln')
    .constant('textEditorConfig', {
        placeholder: 'Add a description',
        toolbar: ['title', 'bold', 'italic', 'underline', 'strikethrough', 'fontScale', 'color',
            'ol', 'ul', 'blockquote', 'table', 'link', 'image', 'hr', 'indent', 'outdent', 'alignment'],
        pasteImage: true,
        defaultImage: 'assets/images/image.gif'
    })
    .directive('myTextEditor', function ($timeout, textEditorConfig) {
        return {
            scope: {
                myModel: '=',
                myReadonly: '='
            },
            restrict: 'E',
            template: '<textarea data-autosave="editor-content" autofocus></textarea>',
            replace: true,
            link: function (scope, elem) {
                Simditor.locale = 'en_EN';
                var editor = new Simditor(
                    angular.extend({textarea: elem}, textEditorConfig)
                );

                var newContent = '';

                scope.$watch('myModel', function (value) {
                    if (typeof value !== 'undefined' && value !== newContent) {
                        editor.setValue(value);
                    }
                });

                editor.on('valuechanged', function () {
                    if (scope.myModel !== editor.getValue()) {
                        $timeout(function () {
                            scope.myModel = newContent = editor.getValue();
                        });
                    }
                });

                if (scope.myReadonly === true) {
                    editor.body.attr('contenteditable', false);
                }
                scope.$watch('myReadonly', function(newValue) {
                    editor.body.attr('contenteditable', !newValue);
                });
            }
        };
    });