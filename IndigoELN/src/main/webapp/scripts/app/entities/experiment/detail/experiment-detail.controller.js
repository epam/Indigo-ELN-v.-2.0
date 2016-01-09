'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', ExperimentDetailController);

ExperimentDetailController.$inject = ['experimentService', '$stateParams'];

function ExperimentDetailController(experimentService, $stateParams) {
    var vm = this;
    vm.experiment = experimentService.get({id: $stateParams.id});
    vm.getIdleWorkdays = getIdleWorkdays;

    function getIdleWorkdays(lastEditDate) {
        var now = new Date();
        var t2 = now.getTime();
        var t1 = lastEditDate.getTime();

        return parseInt((t2 - t1) / (24 * 3600 * 1000));
    }
}
