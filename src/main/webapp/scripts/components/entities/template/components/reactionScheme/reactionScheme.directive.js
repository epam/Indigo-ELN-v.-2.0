'use strict';
angular.module('indigoeln')
    .directive('reactionScheme', function () {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/reactionScheme/reactionScheme.html'
        };
    });