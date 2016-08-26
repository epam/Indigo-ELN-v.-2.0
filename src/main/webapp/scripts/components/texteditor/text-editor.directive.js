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
                myName: '@',
                myModel: '=',
                myReadonly: '='
            },
            require: '^form',
            restrict: 'E',
            template: '<textarea name={{myName}} ng-model="myModel" data-autosave="editor-content" autofocus></textarea>',
            replace: true,
            link: function (scope, elem, iAttrs, formCtrl) {
                Simditor.locale = 'en_EN';
                var editor = new Simditor(
                    angular.extend({textarea: elem}, textEditorConfig)
                );

                var newContent = null;
                var unbinds = [];
                var isInit = false;
                unbinds.push(scope.$watch('myModel', function (value) {
                    if (value && value !== newContent) {
                        editor.setValue(value);
                    }
                    if (isInit) {
                        formCtrl[scope.myName].$setDirty();
                    }
                    isInit = true;
                }));

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
                unbinds.push(scope.$watch('myReadonly', function (newValue) {
                    editor.body.attr('contenteditable', !newValue);
                }));
                scope.$on('$destroy', function () {
                    _.each(unbinds, function (unbind) {
                        unbind();
                    });
                });
            }
        };
    });