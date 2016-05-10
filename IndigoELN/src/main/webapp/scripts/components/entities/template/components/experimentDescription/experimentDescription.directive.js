/* globals $ */
'use strict';
angular.module('indigoeln')
    .directive('experimentDescription', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/experimentDescription/experimentDescription.html'
        };
    });

