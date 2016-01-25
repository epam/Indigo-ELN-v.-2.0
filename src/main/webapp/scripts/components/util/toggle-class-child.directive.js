'use strict';

angular.module('indigoeln').directive('toggleClassChild', toggleClassChild);

function toggleClassChild() {
    return {
        restrict: 'A',
        link: function (scope, element, attrs) {
            var toggledClass = attrs.toggleClassChild;
            element.bind('click', function (e) {
                e.stopPropagation();
                var child = element.find('ul')[0];
                $(child).toggleClass(toggledClass);
            });
        }
    };
}