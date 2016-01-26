'use strict';

angular.module('indigoeln')
    .controller('NewNotebookDialogController', function ($scope, $uibModalInstance) {
        $scope.notebookName = '';
        $scope.ok = okPressed;
        $scope.cancel = cancelPressed;

        function okPressed () {
            $uibModalInstance.close($scope.notebookName);
        }

        function cancelPressed () {
            $uibModalInstance.dismiss();
        }
    });
