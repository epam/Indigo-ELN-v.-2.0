(function() {
    angular
        .module('indigoeln')
        .controller('ExperimentCreationFromNotebookController', ExperimentCreationFromNotebookController);

    /* @ngInject */
    function ExperimentCreationFromNotebookController($rootScope, $stateParams, $uibModalInstance, Experiment, pageInfo) {
        var vm = this;

        vm.experiment = pageInfo.entity;
        vm.notebookId = $stateParams.notebookId;
        vm.projectId = $stateParams.projectId;
        vm.templates = pageInfo.templates;
        vm.mode = pageInfo.mode;
        vm.template = vm.experiment.template;

        vm.ok = save;
        vm.cancel = cancelPressed;

        function save() {
            vm.isSaving = true;
            vm.experiment = _.extend(vm.experiment, {
                template: vm.template
            });
            Experiment.save({
                notebookId: $stateParams.notebookId,
                projectId: $stateParams.projectId
            }, vm.experiment, onSaveSuccess, onSaveError);
        }

        function cancelPressed() {
            window.history.back();
            $uibModalInstance.dismiss();
        }

        function onSaveSuccess(result) {
            vm.isSaving = false;
            $rootScope.$broadcast('experiment-created', {
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId,
                id: result.id
            });
            $uibModalInstance.close({
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId,
                id: result.id
            });
        }

        function onSaveError() {
            vm.isSaving = false;
        }
    }
})();
