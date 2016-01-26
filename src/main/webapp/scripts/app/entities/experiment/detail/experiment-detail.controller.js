'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', function(Experiment, $stateParams) {
        var vm = this;
        vm.experiment = Experiment.get({id: $stateParams.id});
        vm.getIdleWorkdays = getIdleWorkdays;

        function getIdleWorkdays(lastEditDate) {
            var now = new Date();
            var t2 = now.getTime();
            var t1 = lastEditDate.getTime();

            return parseInt((t2 - t1) / (24 * 3600 * 1000));
        }
    });
