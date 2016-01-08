'use strict';

angular.module('indigoeln')
    .controller('ExperimentDetailController', ExperimentDetailController);

ExperimentDetailController.$inject = ['experimentService', '$stateParams'];

function ExperimentDetailController(experimentService, $stateParams) {
    var vm = this;
    vm.experiment = experimentService.get({id: $stateParams.id});
    vm.load = function (id) {
        experimentService.get({id: id}, function (result) {
            vm.experiment = result;
        });
    };
}
