(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentDeleteController', ExperimentDeleteController);

    /* @ngInject */
    function ExperimentDeleteController($uibModalInstance, $stateParams, entity, Experiment) {
        var vm = this;
        vm.experiment = entity;

        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete(id) {
            Experiment.delete({
                experimentId: id,
                notebookId: $stateParams.notebookId,
                projectId: $stateParams.projectId
            },
                function() {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
