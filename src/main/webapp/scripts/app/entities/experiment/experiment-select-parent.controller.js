'use strict';

angular.module('indigoeln')
    .controller('ExperimentSelectParentController', function ($scope, $uibModalInstance, parents) {
        $scope.parents = parents;
        $scope.selectedParent = '';

        $scope.ok = okPressed;
        $scope.cancel = cancelPressed;

        function okPressed() {
            $uibModalInstance.close({notebookId: $scope.selectedParent.id, projectId: $scope.selectedParent.parentId});
        }

        function cancelPressed() {
            $uibModalInstance.dismiss();
        }
    });
