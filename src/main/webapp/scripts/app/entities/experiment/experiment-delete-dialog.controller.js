angular.module('indigoeln')
    .controller('ExperimentDeleteController', function ($scope, $uibModalInstance, entity, Experiment, $stateParams) {
        var vm = this;

        vm.confirmationTitle = 'Confirm delete operation';
        vm.confirmationQuestion = 'Are you sure you want to delete this Experiment?';
        vm.cancelButtonText = 'Cancel';
        vm.deleteButtonText = 'Delete';

        $scope.experiment = entity;
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.confirmDelete = function (id) {
            Experiment.delete({experimentId: id, notebookId: $stateParams.notebookId, projectId: $stateParams.projectId},
                function () {
                    $uibModalInstance.close(true);
                });
        };

    });
