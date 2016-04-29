angular.module('indigoeln')
    .controller('NotebookSelectParentController', function ($scope, $uibModalInstance, parents) {
        $scope.parents = parents;
        $scope.selectedParent = '';

        $scope.ok = okPressed;
        $scope.cancel = cancelPressed;

        function okPressed () {
            $uibModalInstance.close($scope.selectedParent.id);
        }

        function cancelPressed () {
            $uibModalInstance.dismiss();
        }
    });
