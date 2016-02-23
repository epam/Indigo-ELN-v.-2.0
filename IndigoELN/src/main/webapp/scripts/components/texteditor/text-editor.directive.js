'use strict';

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
                content: '=',
                myReadonly: '='
            },
            restrict: 'E',
            template: '<textarea data-autosave="editor-content" autofocus></textarea>',
            replace: true,
            link: function (scope, elem, attrs) {
                Simditor.locale = 'en_EN';
                var editor = new Simditor(
                    angular.extend({textarea: elem}, textEditorConfig)
                );

                var newContent = '';

                scope.$watch('content', function (value, old) {
                    if (typeof value !== 'undefined' && value !== newContent) {
                        editor.setValue(value);
                    }
                });

                editor.on('valuechanged', function (e) {
                    if (scope.content !== editor.getValue()) {
                        $timeout(function () {
                            scope.content = newContent = editor.getValue();
                        });
                    }
                });
                if (scope.myReadonly === true) {
                    editor.body.attr('contenteditable', false);
                }
            }
        };
    });