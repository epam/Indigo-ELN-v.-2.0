'use strict';

angular.module('indigoeln')
    .directive('onReadFile', function ($parse) {
        return {
            restrict: 'A',
            scope: false,
            controller: 'ImportController',
            link: function (scope, element, attrs) {

                var showFunc = $parse(attrs.onReadFile);

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
        }
    })
    .controller("ImportController", function ($scope) {

        $scope.showContent = function($fileContent) {
            $scope.content = $fileContent;
        };

    });