'use strict';

angular.module('indigoeln')
    .directive('myTextEditor', function() {
        return {
            require: '?^ngModel',
            link: function (scope, element, attrs, ngModel) {
                element.append('<div style="height:300px;"></div>');
                var TOOLBAR_DEFAULT = ['title','bold','italic','underline','strikethrough','fontScale','color',
                    'ol','ul','blockquote','table','link','image','hr','indent','outdent','alignment'];
                var toolbar = scope.$eval(attrs.toolbar) || TOOLBAR_DEFAULT;
                Simditor.locale = 'en_EN';
                scope.editor = new Simditor({
                    textarea: element,
                    pasteImage: true,
                    toolbar: toolbar,
                    defaultImage: 'assets/images/image.gif'
                    //fileKey: 'upload_file',
                    //upload: {
                    //    url: '/api/project_files/123abc',
                    //    params: {'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')}
                    //}
                });

                var $target = scope.editor.body;

                function readViewText() {
                    ngModel.$setViewValue($target.html());
                    if (attrs.ngRequired !== undefined && attrs.ngRequired !== 'false') {
                        var text = $target.text();
                        if(text.trim() === '') {
                            ngModel.$setValidity('required', false);
                        } else {
                            ngModel.$setValidity('required', true);
                        }
                    }
                }

                ngModel.$render = function () {
                    scope.editor.focus();
                    $target.html(ngModel.$viewValue);
                };

                scope.editor.on('valuechanged', function () {
                    scope.$apply(readViewText);
                });
            }
        };
    });
