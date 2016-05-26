angular.module('indigoeln')
    .controller('ExperimentSelectParentTemplateController', function ($scope, $rootScope, $state, $uibModalInstance, parents, templates, Experiment) {
        $scope.parents = parents;
        $scope.selectedParent = '';
        $scope.templates = templates;
        $scope.selectedTemplate = '';

        $scope.experiment = {name: null, experimentNumber: null, template: null, id: null};

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $rootScope.$broadcast('experiment-created', {
                projectId: $scope.selectedParent.parentId,
                notebookId: $scope.selectedParent.id,
                id: result.id
            });
            $uibModalInstance.close({
                projectId: $scope.selectedParent.parentId,
                notebookId: $scope.selectedParent.id,
                id: result.id
            });
        };

        var onSaveError = function () {
            $scope.isSaving = false;
        };

        function save() {
            $scope.isSaving = true;
            $scope.experiment = _.extend($scope.experiment, {template: $scope.selectedTemplate});
            Experiment.save({
                notebookId: $scope.selectedParent.id,
                projectId: $scope.selectedParent.parentId
            }, $scope.experiment, onSaveSuccess, onSaveError);
        }

        function cancelPressed() {
            $uibModalInstance.dismiss();
        }

        $scope.ok = save;
        $scope.cancel = cancelPressed;

    });
