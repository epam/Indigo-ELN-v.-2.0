'use strict';

angular.module('indigoeln')
    .directive('myFileReader', function ($parse) {
        return {
            restrict: 'A',
            scope: false,
            controller: function ($scope) {
                $scope.showContent = function($fileContent) {
                    $scope.content = $fileContent;
                };
            },
            link: function (scope, element, attrs) {

                var showFunc = $parse(attrs.myFileReader);

                element.on('change', function (onChangeEvent) {
                    var reader = new FileReader();
                    reader.onload = function (onLoadEvent) {
                        scope.$apply(function () {
                            showFunc(scope, {$fileContent: onLoadEvent.target.result});
                        });
                    };
                    reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
                });
            }
        };
    });