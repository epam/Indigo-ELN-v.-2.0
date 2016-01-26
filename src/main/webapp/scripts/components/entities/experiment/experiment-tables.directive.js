'use strict';

angular.module('indigoeln')
    .directive('experimentTables', function () {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/entities/experiment/table/experiment-tables.html',
            controller: 'ExperimentTablesController'
        };
    });