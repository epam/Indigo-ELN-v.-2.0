'use strict';
angular.module('indigoeln').
    directive('keyLogger', function() {
    return {
        restrict: 'A',
        link: function postLink(scope, iElement, iAttrs){
            angular.element(document).on('keypress', function(e){
                if (scope.canSort) {
                    scope.$apply(scope.keyPressed(e));
                }
            });

            scope.$on('$destroy', function () {
                angular.element(document).off('keypress');
            });
        }
    };
});
