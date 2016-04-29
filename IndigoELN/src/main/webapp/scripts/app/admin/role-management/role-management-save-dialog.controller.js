angular.module('indigoeln')
    .controller('role-managementSaveController', function ($scope, $uibModalInstance) {
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.confirmSave = function () {
            $uibModalInstance.close(true);
        };
    });