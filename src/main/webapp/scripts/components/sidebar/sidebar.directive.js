angular.module('indigoeln')
    .directive('uuiToggleBox', function () {
    return {
        restrict: 'C',
        link: function (scope, element) {
            element.on('click', function () {
                var $main = $('.main-container');
                $main.toggleClass('hide-menu');
                $main.toggleClass('open');
            });
        }
    };
    });
