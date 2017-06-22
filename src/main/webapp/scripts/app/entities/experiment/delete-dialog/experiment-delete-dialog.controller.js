(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentDeleteController', ExperimentDeleteController);

    /* @ngInject */
    function ExperimentDeleteController($uibModalInstance, $stateParams, entity, Experiment) {
        var self = this;
        self.experiment = entity;

        self.clear          = clear;
        self.confirmDelete  = confirmDelete;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete(id) {
            Experiment.delete({
                    experimentId: id,
                    notebookId: $stateParams.notebookId,
                    projectId: $stateParams.projectId
                },
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();