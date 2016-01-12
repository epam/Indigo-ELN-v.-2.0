'use strict';

angular.module('indigoeln').directive('toggleClass', toggleClass);

function toggleClass() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var toggledClass = attrs.toggleClass;
            element.bind('click', function() {
                element.toggleClass(attrs.toggleClass);
            });
        }
    };
}