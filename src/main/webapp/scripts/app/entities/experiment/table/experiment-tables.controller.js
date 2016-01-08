(function () {
    'use strict';

    angular
        .module('indigoeln')
        .controller('ExperimentTablesController', ExperimentTablesController);

    ExperimentTablesController.$inject = ['experimentTablesService'];

    function ExperimentTablesController(experimentTablesService) {
        var vm = this;
        vm.experiments = experimentTablesService.get();
    }

})();