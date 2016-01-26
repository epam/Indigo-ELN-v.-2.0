'use strict';

angular.module('indigoeln')
    .controller('ExperimentTablesController', function($scope, ExperimentTables) {
        $scope.experiments = ExperimentTables.get();
        $scope.getIdleWorkdays = getIdleWorkdays;

        function getIdleWorkdays(lastEditDate) {
            var now = new Date();
            var t2 = now.getTime();
            var t1 = lastEditDate.getTime();

            return parseInt((t2 - t1) / (24 * 3600 * 1000));
        }
    });