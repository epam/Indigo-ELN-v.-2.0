angular.module('indigoeln')
    .directive('attachments', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/attachments/attachments.html'
        };
    });