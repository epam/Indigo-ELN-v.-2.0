angular.module('indigoeln').controller('StructureValidationController',
    function ($scope, $uibModalInstance, batches, searchQuery, AppValues) {
        $scope.batches = batches;
        $scope.searchQuery = searchQuery;
        $scope.selectedBatch = null;
        $scope.defaultSaltCodeName = AppValues.getDefaultSaltCode().name;

        $scope.selectBatch = function (batch) {
            batch.$$isSelected = !batch.$$isSelected;
            if (batch.$$isSelected) {
                $scope.selectedBatch = batch;
            } else {
                $scope.selectedBatch = null;
            }
        };

        $scope.save = function () {
            $uibModalInstance.close($scope.selectedBatch);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

