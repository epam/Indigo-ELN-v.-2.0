'use strict';

angular
    .module('indigoeln')
    .directive('experimentTables', experimentTables);

experimentTables.$inject = ['experimentService'];

function experimentTables(experimentService) {
    return {
        restrict: 'E',
        templateUrl: 'scripts/app/entities/experiment/table/experiment-tables.html',
        controller: 'ExperimentTablesController',
        controllerAs: 'vm',
        bindToController: true
    };
};