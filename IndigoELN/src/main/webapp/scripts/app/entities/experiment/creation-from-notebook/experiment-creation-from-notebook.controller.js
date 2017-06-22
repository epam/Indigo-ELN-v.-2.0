(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentCreationFromNotebookController', ExperimentCreationFromNotebookController);

    /* @ngInject */
    function ExperimentCreationFromNotebookController($rootScope, $stateParams, $uibModalInstance, Experiment, pageInfo) {
        var self = this;

        self.experiment = pageInfo.entity;
        self.notebookId = $stateParams.notebookId;
        self.projectId = $stateParams.projectId;
        self.templates = pageInfo.templates;
        self.mode = pageInfo.mode;
        self.template = self.experiment.template;

        self.ok     = save;
        self.cancel = cancelPressed;

        function save() {
            self.isSaving = true;
            self.experiment = _.extend(self.experiment, {template: self.template});
            Experiment.save({
                notebookId: $stateParams.notebookId,
                projectId: $stateParams.projectId
            }, self.experiment, onSaveSuccess, onSaveError);
        }

        function cancelPressed() {
            window.history.back();
            $uibModalInstance.dismiss();
        }

        function onSaveSuccess(result) {
            self.isSaving = false;
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
            self.isSaving = false;
        }
    }
})();
