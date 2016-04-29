angular.module('indigoeln')
    .directive('sResizable', function () {
        return {
            restrict: 'A',
            scope: {
                maxHeight: '=',
                minHeight: '='
            },
            link: function (scope, elem) {
                elem.resizable({
                    handles: 's',
                    maxHeight: scope.maxHeight,
                    minHeight: scope.minHeight
                });
            }
        };
    });