/* @ngInject */
function ExperimentDeleteController($uibModalInstance, $stateParams, entity, experimentService) {
    var vm = this;
    vm.experiment = entity;

    vm.clear = clear;
    vm.confirmDelete = confirmDelete;

    function clear() {
        $uibModalInstance.dismiss('cancel');
    }

    function confirmDelete(id) {
        experimentService.delete({
                experimentId: id,
                notebookId: $stateParams.notebookId,
                projectId: $stateParams.projectId
            },
            function() {
                $uibModalInstance.close(true);
            });
    }
}

module.exports = ExperimentDeleteController;
