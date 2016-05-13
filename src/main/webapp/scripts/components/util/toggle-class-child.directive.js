angular.module('indigoeln')
    .directive('toggleClassChild', function () {
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
    });