(function() {
    angular
        .module('indigoeln')
        .directive('indigoTextEditor', indigoTextEditor);

    /* @ngInject */
    function indigoTextEditor($timeout) {
        return {
            scope: {
                indigoName: '@',
                indigoModel: '=',
                indigoReadonly: '='
            },
            require: '^form',
            restrict: 'E',
            template: '<textarea name={{indigoName}} class="form-control" ng-model="indigoModel" data-autosave="editor-content" autofocus></textarea>',
            replace: true,
            link: link
        };

        /* @ngInject */
        function link(scope, elem, iAttrs, formCtrl, textEditorConfig) {
            Simditor.locale = 'en_EN';
            var editor = new Simditor(
                angular.extend({
                    textarea: elem
                }, textEditorConfig)
            );

            var newContent = null;
            var unbinds = [];
            var isInit = false;
            unbinds.push(scope.$watch('indigoModel', function(value) {
                if (value && value !== newContent) {
                    editor.setValue(value);
                }
                if (isInit) {
                    formCtrl[scope.indigoName].$setDirty();
                }
                isInit = true;
            }));

            editor.on('valuechanged', function() {
                if (scope.indigoModel !== editor.getValue()) {
                    $timeout(function() {
                        scope.indigoModel = newContent = editor.getValue();
                        if (isInit) {
                            formCtrl[scope.indigoName].$setDirty();
                        }
                    });
                }
            });

            if (scope.indigoReadonly === true) {
                editor.body.attr('contenteditable', false);
            }
            unbinds.push(scope.$watch('indigoReadonly', function(newValue) {
                editor.body.attr('contenteditable', !newValue);
            }));
            scope.$on('$destroy', function() {
                _.each(unbinds, function(unbind) {
                    unbind();
                });
            });
        }
    }
})();
